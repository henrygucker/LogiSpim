"""Platform-independent GNU binutils build orchestration and extension points."""

from __future__ import annotations

import hashlib
import os
import re
import shutil
import tarfile
import urllib.request
from pathlib import Path, PurePosixPath

from ..InstallHelper.install_helper import BuildError, InstallHelper


class BuildHelper(InstallHelper):
    """Coordinate source retrieval, configuration, compilation, and verification.

    The helper receives its prerequisite installer as a dependency. The base
    class implements only host-independent work; host-dependent extraction,
    process launching, permission handling, and runtime-library checks are
    explicit extension points that raise ``NotImplementedError`` until a
    platform-specific build helper supplies them.
    """

    TARGET_TRIPLET = "mipsel-none-elf"
    GNU_DOWNLOAD_ROOT = "https://ftp.gnu.org/gnu/binutils"
    REQUIRED_TOOLS = (
        "as", "ld", "ar", "nm", "objcopy", "objdump", "readelf", "size", "strings", "strip"
    )

    def __init__(self, binutils_dir: Path, install_helper: InstallHelper, host_tag: str) -> None:
        """Store project paths, the injected installer, and the selected host tag."""
        self.binutils_dir = binutils_dir.resolve()
        self.install_helper = install_helper
        self.host_tag = host_tag
        self.target_dir = self.binutils_dir / "target"
        self.source_root = self.target_dir / "src"
        self.build_root = self.target_dir / "build"
        self.output_dir = self.target_dir / "binutils"
        self.build_environment: dict[str, str] = os.environ.copy()

    def install_prerequisites(self) -> dict[str, str]:
        """Delegate prerequisite installation to the injected platform installer."""
        return self.install_helper.install_prerequisites()

    def build(self) -> None:
        """Run the complete build pipeline and verify the installed MIPS binutils tools."""
        self.log(f"Host: {self.host_tag}")
        self.log(f"Target: {self.TARGET_TRIPLET}")
        environment = self.install_prerequisites()
        self.build_environment.update(environment)
        version, checksum = self.read_version_and_checksum()
        self.log(f"GNU binutils version: {version}")
        self.create_build_directories()
        source_dir = self.prepare_source(version, checksum)
        self.reset_build_and_output_directories()
        self.execute_build_script(source_dir, version)
        self.verify_installation(version)
        self.log("Build completed successfully.")

    def read_version_and_checksum(self) -> tuple[str, str]:
        """Read and validate the pinned GNU Binutils version and SHA-256 digest."""
        version_file = self.binutils_dir / ".gnu_version"
        checksum_file = self.binutils_dir / ".gnu_sha256"
        if not version_file.is_file():
            raise BuildError(f"Missing GNU Binutils version file: {version_file}")
        if not checksum_file.is_file():
            raise BuildError(f"Missing GNU Binutils checksum file: {checksum_file}")
        version = version_file.read_text(encoding="utf-8").strip()
        checksum = checksum_file.read_text(encoding="utf-8").strip().lower()
        if not version or not re.fullmatch(r"[0-9]+(?:\.[0-9]+)+", version):
            raise BuildError(f"Invalid GNU Binutils version in {version_file}: {version!r}")
        if not re.fullmatch(r"[0-9a-f]{64}", checksum):
            raise BuildError(f"{checksum_file} must contain exactly one SHA-256 digest.")
        return version, checksum

    def create_build_directories(self) -> None:
        """Create the source, intermediate-build, and installation directories."""
        self.source_root.mkdir(parents=True, exist_ok=True)
        self.build_root.mkdir(parents=True, exist_ok=True)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def reset_build_and_output_directories(self) -> None:
        """Remove stale build and output files without deleting the cached source archive."""
        for directory in (self.build_root, self.output_dir):
            if directory.exists():
                shutil.rmtree(directory)
            directory.mkdir(parents=True, exist_ok=True)

    def download_file(self, url: str, destination: Path) -> None:
        """Download a URL to a temporary file and atomically move it into place."""
        destination.parent.mkdir(parents=True, exist_ok=True)
        temporary = destination.with_suffix(destination.suffix + ".part")
        request = urllib.request.Request(url, headers={"User-Agent": "LogiSpim-binutils-builder/2.0"})
        self.log(f"Downloading {url}")
        try:
            with urllib.request.urlopen(request, timeout=120) as response, temporary.open("wb") as output:
                shutil.copyfileobj(response, output)
            temporary.replace(destination)
        except Exception:
            temporary.unlink(missing_ok=True)
            raise

    @staticmethod
    def sha256_file(path: Path) -> str:
        """Calculate a file's SHA-256 digest using bounded-memory streaming."""
        digest = hashlib.sha256()
        with path.open("rb") as source_file:
            for chunk in iter(lambda: source_file.read(1024 * 1024), b""):
                digest.update(chunk)
        return digest.hexdigest()

    def prepare_source(self, version: str, expected_checksum: str) -> Path:
        """Download, verify, and extract the pinned GNU Binutils source archive."""
        archive = self.source_root / f"binutils-{version}.tar.xz"
        source_dir = self.source_root / f"binutils-{version}"
        if archive.exists() and self.sha256_file(archive) != expected_checksum:
            self.log("Cached GNU source archive failed checksum verification; downloading a fresh copy.")
            archive.unlink()
        if not archive.exists():
            url = f"{self.GNU_DOWNLOAD_ROOT}/binutils-{version}.tar.xz"
            try:
                self.download_file(url, archive)
            except Exception as exc:
                raise BuildError(f"Unable to download GNU Binutils {version}: {exc}") from exc
        actual_checksum = self.sha256_file(archive)
        if actual_checksum != expected_checksum:
            archive.unlink(missing_ok=True)
            raise BuildError(
                "GNU Binutils source checksum mismatch. The downloaded archive was deleted; "
                "check .gnu_version and .gnu_sha256 against the official GNU release information."
            )
        self.log(f"Verified SHA-256 for GNU Binutils {version}.")

        if not (source_dir / "configure").is_file():
            if source_dir.exists():
                shutil.rmtree(source_dir)
            self.validate_source_archive(archive)
            self.extract_source_archive(archive, self.source_root)
        if not (source_dir / "configure").is_file():
            raise BuildError(f"Expected GNU configure script was not found: {source_dir / 'configure'}")
        return source_dir

    def validate_source_archive(self, archive: Path) -> None:
        """Reject archive entries and symbolic links that could escape the source directory."""
        extraction_root = self.source_root.resolve()
        try:
            with tarfile.open(archive, mode="r:xz") as tar_archive:
                for member in tar_archive.getmembers():
                    member_path = PurePosixPath(member.name)
                    if member_path.is_absolute() or ".." in member_path.parts:
                        raise BuildError(f"Unsafe path in GNU source archive: {member.name!r}")
                    resolved = (self.source_root / Path(*member_path.parts)).resolve()
                    try:
                        resolved.relative_to(extraction_root)
                    except ValueError as exc:
                        raise BuildError(f"Archive entry escapes source directory: {member.name!r}") from exc
                    if member.issym() or member.islnk():
                        link_path = PurePosixPath(member.linkname)
                        if link_path.is_absolute():
                            raise BuildError(f"Unsafe archive link: {member.name!r} -> {member.linkname!r}")
                        link_base = member_path.parent if member.issym() else PurePosixPath()
                        resolved_link = (self.source_root / Path(*(link_base / link_path).parts)).resolve()
                        try:
                            resolved_link.relative_to(extraction_root)
                        except ValueError as exc:
                            raise BuildError(
                                f"Archive link escapes source directory: {member.name!r} -> {member.linkname!r}"
                            ) from exc
        except (tarfile.TarError, OSError) as exc:
            raise BuildError(f"Unable to inspect GNU source archive {archive}: {exc}") from exc

    def extract_source_archive(self, archive: Path, destination: Path) -> None:
        """Extract the source archive using the selected host's filesystem semantics."""
        raise NotImplementedError(
            f"{type(self).__name__}.extract_source_archive() must implement host-specific extraction."
        )

    def execute_build_script(self, source_dir: Path, version: str) -> None:
        """Configure, compile, and install binutils using the repository's Bash script."""
        raise NotImplementedError(
            f"{type(self).__name__}.execute_build_script() must implement host-specific process launching."
        )

    def apply_executable_permissions(self, bin_directory: Path) -> None:
        """Apply the executable permissions appropriate for the host operating system."""
        raise NotImplementedError(
            f"{type(self).__name__}.apply_executable_permissions() must implement host permissions."
        )

    def verify_host_runtime_dependencies(self, bin_directory: Path) -> None:
        """Verify or bundle host-specific shared-library dependencies of installed executables."""
        raise NotImplementedError(
            f"{type(self).__name__}.verify_host_runtime_dependencies() must implement host runtime checks."
        )

    def verify_installation(self, version: str) -> None:
        """Check required tools, host runtime dependencies, version, and MIPS object support."""
        bin_directory = self.output_dir / "bin"
        suffix = ".exe" if self.host_tag.startswith("windows-") else ""
        missing = [
            bin_directory / f"{self.TARGET_TRIPLET}-{tool}{suffix}"
            for tool in self.REQUIRED_TOOLS
            if not (bin_directory / f"{self.TARGET_TRIPLET}-{tool}{suffix}").is_file()
        ]
        if missing:
            formatted = "\n".join(f"  - {path}" for path in missing)
            raise BuildError(f"The build completed, but expected GNU binutils tools are missing:\n{formatted}")

        self.apply_executable_permissions(bin_directory)
        self.verify_host_runtime_dependencies(bin_directory)

        assembler = bin_directory / f"{self.TARGET_TRIPLET}-as{suffix}"
        result = self.run_command([str(assembler), "--version"], check=False, capture_output=True)
        if result.returncode != 0 or version not in (result.stdout + result.stderr):
            raise BuildError(f"Installed GNU assembler failed its version check: {assembler}")

        objdump = bin_directory / f"{self.TARGET_TRIPLET}-objdump{suffix}"
        result = self.run_command([str(objdump), "-i"], check=False, capture_output=True)
        if result.returncode != 0 or "mips" not in (result.stdout + result.stderr).lower():
            raise BuildError("Installed objdump did not report MIPS target support.")
        self.log(f"Verified GNU Binutils {version} installation.")
        self.log(f"Installed executables: {bin_directory}")


class PosixBuildHelper(BuildHelper):
    """Shared Bash, tar extraction, and execute-bit implementation for macOS and Linux."""

    def extract_source_archive(self, archive: Path, destination: Path) -> None:
        """Extract the already-validated GNU source tarball with Python's tarfile module."""
        destination.mkdir(parents=True, exist_ok=True)
        try:
            with tarfile.open(archive, mode="r:xz") as tar_archive:
                tar_archive.extractall(destination, members=tar_archive.getmembers())
        except (tarfile.TarError, OSError) as exc:
            raise BuildError(f"Unable to extract GNU source archive {archive}: {exc}") from exc

    def execute_build_script(self, source_dir: Path, version: str) -> None:
        """Run the repository Bash build script in the native POSIX environment."""
        script = self.binutils_dir / "scripts" / "build-binutils.sh"
        if not script.is_file():
            raise BuildError(f"Missing GNU Binutils build script: {script}")
        environment = self.build_environment.copy()
        environment.update(
            {
                "BINUTILS_SOURCE_DIR": str(source_dir),
                "BINUTILS_BUILD_DIR": str(self.build_root),
                "BINUTILS_OUTPUT_DIR": str(self.output_dir),
                "BINUTILS_TARGET": self.TARGET_TRIPLET,
                "BINUTILS_VERSION": version,
                "JOBS": str(max(1, min(os.cpu_count() or 2, 4))),
                "MAKEINFO": "true",
            }
        )
        self.run_command(
            ["bash", str(script)],
            env=environment,
            cwd=self.binutils_dir,
            description=f"Building GNU Binutils {version} for {self.TARGET_TRIPLET} ({self.host_tag})",
        )

    def apply_executable_permissions(self, bin_directory: Path) -> None:
        """Set execute bits on all installed target-prefixed POSIX executables."""
        for executable in bin_directory.glob(f"{self.TARGET_TRIPLET}-*"):
            if executable.is_file():
                executable.chmod(executable.stat().st_mode | 0o111)

    def verify_host_runtime_dependencies(self, bin_directory: Path) -> None:
        """No additional runtime-library packaging is required by the POSIX build helper."""
        return None
