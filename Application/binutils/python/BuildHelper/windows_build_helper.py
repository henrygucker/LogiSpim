"""Host-native Windows PE build helper using the MSYS2 UCRT64 toolchain."""

from __future__ import annotations

import os
import re
import shlex
import shutil
import subprocess
from pathlib import Path

from .build_helper import BuildHelper
from ..InstallHelper.install_helper import BuildError, InstallHelper


class WindowsBuildHelper(BuildHelper):
    """Build native Windows executables with MinGW-w64 from the MSYS2 UCRT64 environment."""

    def __init__(self, binutils_dir: Path, install_helper: InstallHelper, host_tag: str) -> None:
        """Initialize the Windows build helper and clear the unresolved MSYS2 root."""
        super().__init__(binutils_dir, install_helper, host_tag)
        self._msys_root: Path | None = None

    def install_prerequisites(self) -> dict[str, str]:
        """Install Windows prerequisites and remember the MSYS2 installation path."""
        environment = super().install_prerequisites()
        root = environment.get("MSYS2_ROOT")
        if not root:
            raise BuildError("Windows prerequisite installation did not return MSYS2_ROOT.")
        self._msys_root = Path(root)
        return environment

    def extract_source_archive(self, archive: Path, destination: Path) -> None:
        """Extract the validated source archive with MSYS2 tar to preserve POSIX metadata."""
        root = self._require_msys_root()
        archive_path = self._to_msys_path(root, archive)
        destination_path = self._to_msys_path(root, destination)
        command = (
            f"mkdir -p {shlex.quote(destination_path)} && "
            f"tar -xf {shlex.quote(archive_path)} -C {shlex.quote(destination_path)}"
        )
        self._run_msys(root, command, description="Extracting GNU source with MSYS2 tar")

    def execute_build_script(self, source_dir: Path, version: str) -> None:
        """Run build-binutils.sh inside UCRT64 Bash, producing native Windows PE tools."""
        root = self._require_msys_root()
        script = self.binutils_dir / "scripts" / "build-binutils.sh"
        if not script.is_file():
            raise BuildError(f"Missing GNU Binutils build script: {script}")
        script_path = self._to_msys_path(root, script)
        jobs = max(1, min(os.cpu_count() or 2, 4))
        self._run_msys(
            root,
            f"JOBS={jobs} MAKEINFO=true bash {shlex.quote(script_path)}",
            description=f"Building GNU Binutils {version} for {self.TARGET_TRIPLET} ({self.host_tag})",
        )

    def apply_executable_permissions(self, bin_directory: Path) -> None:
        """Do nothing because native Windows PE executables do not use POSIX execute bits."""
        return None

    def verify_host_runtime_dependencies(self, bin_directory: Path) -> None:
        """Copy MinGW DLL dependencies beside the tools and reject MSYS/Cygwin runtime imports."""
        root = self._require_msys_root()
        runtime_directory = root / "ucrt64" / "bin"
        objdump = runtime_directory / "objdump.exe"
        if not objdump.is_file():
            raise BuildError(f"Cannot inspect Windows executable imports; missing {objdump}")

        system_root = Path(os.environ.get("SystemRoot", r"C:\Windows"))
        system32 = system_root / "System32"
        pending = list(bin_directory.glob("*.exe"))
        visited: set[Path] = set()
        while pending:
            executable = pending.pop()
            resolved = executable.resolve()
            if resolved in visited:
                continue
            visited.add(resolved)
            result = self.run_command(
                [str(objdump), "-p", str(executable)],
                check=False,
                capture_output=True,
                description=f"Inspecting Windows DLL imports for {executable.name}",
            )
            if result.returncode != 0:
                raise BuildError(f"Unable to inspect DLL imports for {executable.name}: {result.stderr.strip()}")
            imports = re.findall(r"DLL Name:\s*([^\s]+)", result.stdout, flags=re.IGNORECASE)
            for imported_name in imports:
                dll_name = imported_name.strip()
                lower_name = dll_name.lower()
                if lower_name in {"msys-2.0.dll", "cygwin1.dll"}:
                    raise BuildError(
                        f"{executable.name} imports {dll_name}; packaged Windows tools must not depend on MSYS/Cygwin."
                    )
                if lower_name.startswith(("api-ms-win-", "ext-ms-win-")):
                    continue
                if (system32 / dll_name).is_file():
                    continue
                runtime_dll = runtime_directory / dll_name
                if not runtime_dll.is_file():
                    raise BuildError(
                        f"{executable.name} imports {dll_name}, but it is neither a Windows system DLL "
                        f"nor available in {runtime_directory}. Resolve this runtime dependency before packaging."
                    )
                bundled_dll = bin_directory / dll_name
                if not bundled_dll.exists():
                    shutil.copy2(runtime_dll, bundled_dll)
                    self.log(f"Bundled Windows runtime dependency: {dll_name}")
                pending.append(bundled_dll)

    def _require_msys_root(self) -> Path:
        """Return the selected MSYS2 root or fail with a configuration error."""
        if self._msys_root is None:
            raise BuildError("MSYS2 root is unavailable; Windows prerequisite installation did not run.")
        return self._msys_root

    def _to_msys_path(self, root: Path, path: Path) -> str:
        """Convert a Windows filesystem path into MSYS2's POSIX path format."""
        cygpath = root / "usr" / "bin" / "cygpath.exe"
        result = self.run_command(
            [str(cygpath), "-u", str(path)],
            check=True,
            capture_output=True,
            description=f"Converting Windows path for MSYS2: {path}",
        )
        return result.stdout.strip()

    def _run_msys(self, root: Path, command: str, *, description: str) -> subprocess.CompletedProcess[str]:
        """Run a shell command in a new MSYS2 UCRT64 login shell."""
        bash = root / "usr" / "bin" / "bash.exe"
        environment = os.environ.copy()
        environment.update(self.build_environment)
        environment["MSYSTEM"] = "UCRT64"
        environment["CHERE_INVOKING"] = "yes"
        environment["MSYS2_PATH_TYPE"] = "inherit"
        environment["MSYS2_ROOT"] = str(root)
        environment["PATH"] = os.pathsep.join(
            [str(root / "ucrt64" / "bin"), str(root / "usr" / "bin"), environment.get("PATH", "")]
        )
        return self.run_command(
            [str(bash), "-lc", command],
            env=environment,
            check=True,
            description=description,
        )
