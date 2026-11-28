"""MSYS2 UCRT64 bootstrap and prerequisite installation for Windows x86-64."""

from __future__ import annotations

import base64
import os
import platform
import shutil
import subprocess
import sys
import tempfile
import urllib.request
from pathlib import Path

from .install_helper import BuildError, InstallHelper


MSYS2_INSTALLER_URL = (
    "https://github.com/msys2/msys2-installer/releases/download/"
    "nightly-x86_64/msys2-base-x86_64-latest.sfx.exe"
)
MSYS2_PACKAGES = (
    "base-devel",
    "mingw-w64-ucrt-x86_64-gcc",
    "mingw-w64-ucrt-x86_64-binutils",
    # Binutils' configure scripts deliberately disable Info documentation
    # only when a sufficiently recent GNU makeinfo is detected. The
    # environment variable MAKEINFO=true is not a reliable substitute.
    "mingw-w64-ucrt-x86_64-texinfo",
)


class WindowsInstallHelper(InstallHelper):
    """Install or reuse MSYS2 and configure its native UCRT64 build environment."""

    def __init__(self, *, elevated_relaunch: bool = False) -> None:
        """Record whether this process was restarted with the Windows UAC token."""
        self._elevated_relaunch = elevated_relaunch
        self._msys_root: Path | None = None

    def install_prerequisites(self) -> dict[str, str]:
        """Install all missing Windows prerequisites and return MSYS2 environment variables."""
        return self._install_prerequisites(non_interactive=self.is_github_actions())

    def _install_prerequisites(self, *, non_interactive: bool) -> dict[str, str]:
        """Perform the Windows bootstrap, requesting consent/elevation only when needed."""
        machine = platform.machine().lower()
        if machine not in {"amd64", "x86_64"}:
            raise BuildError(f"Windows builds currently support x86-64 only; detected {machine}.")

        root = self._find_msys2_root()
        if root is None:
            missing = ["MSYS2 (UCRT64 environment)"]
            if not self.approve_installation(missing, non_interactive=non_interactive):
                raise BuildError("MSYS2 installation was declined; no build was performed.")
            self._ensure_administrator("Installing MSYS2")
            root = self._download_and_install_msys2()

        self._msys_root = root
        if not self._ucrt64_tools_available(root):
            missing = [
                "MSYS2 UCRT64 build tools (GCC/G++, GNU Make, binutils, and GNU Texinfo/makeinfo)",
                f"MSYS2 packages: {', '.join(MSYS2_PACKAGES)}",
            ]
            if not self.approve_installation(missing, non_interactive=non_interactive):
                raise BuildError("MSYS2 dependency installation was declined; no build was performed.")
            self._ensure_administrator("Installing MSYS2 UCRT64 build packages")
            self._update_msys2(root)
            self._install_ucrt64_packages(root)

        if not self._ucrt64_tools_available(root):
            raise BuildError(
                "MSYS2 setup completed, but one or more required UCRT64 tools are still unavailable: "
                "gcc, g++, make, objdump, or makeinfo."
            )
        self.log(f"Using MSYS2 UCRT64 at {root}")
        return self._msys_environment(root)

    def _find_msys2_root(self) -> Path | None:
        """Locate an existing MSYS2 installation in conventional or configured paths."""
        candidates: list[Path] = []
        for variable in ("MSYS2_ROOT", "MSYS2_PATH"):
            value = os.environ.get(variable)
            if value:
                candidates.append(Path(value))
        candidates.extend(
            [
                Path("C:/msys64"),
                Path("C:/tools/msys64"),
                Path(os.environ.get("LOCALAPPDATA", "C:/Users/Default/AppData/Local"))
                / "Programs"
                / "msys64",
            ]
        )
        for candidate in candidates:
            if (candidate / "usr" / "bin" / "bash.exe").is_file():
                return candidate.resolve()
        return None

    def _ensure_administrator(self, operation: str) -> None:
        """Restart the complete build under the Windows UAC administrator token if needed."""
        if self.is_administrator():
            return
        if self.is_github_actions():
            # Hosted Windows runners normally expose an administrator token. Avoid
            # prompting in CI; attempt the package operation and report its failure.
            self.log(f"GitHub Actions process is not elevated; attempting {operation} without UAC.")
            return
        if self._elevated_relaunch:
            raise BuildError(f"The elevated process does not have administrator privileges for: {operation}")
        self.log(f"Requesting Windows administrator privileges through UAC for: {operation}")
        self._relaunch_elevated()

    def _relaunch_elevated(self) -> None:
        """Relaunch build.py through PowerShell's RunAs verb and propagate its exit code."""
        script = Path(__file__).resolve().parents[2] / "build.py"
        python_executable = Path(sys.executable).resolve()
        ps_source = (
            "$python = '" + str(python_executable).replace("'", "''") + "'; "
            "$script = '" + str(script).replace("'", "''") + "'; "
            "$arguments = '\"' + $script + '\" --elevated-relaunch'; "
            "$child = Start-Process -FilePath $python -ArgumentList $arguments -Verb RunAs -Wait -PassThru; "
            "exit $child.ExitCode"
        )
        encoded = base64.b64encode(ps_source.encode("utf-16le")).decode("ascii")
        result = subprocess.run(
            ["powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encoded],
            check=False,
        )
        raise SystemExit(result.returncode)

    def _download_and_install_msys2(self) -> Path:
        """Download the official MSYS2 bootstrap archive and install it under C:\\msys64."""
        temporary_directory = Path(tempfile.mkdtemp(prefix="binutils-msys2-"))
        installer = temporary_directory / "msys2-base-x86_64-latest.sfx.exe"
        request = urllib.request.Request(
            MSYS2_INSTALLER_URL,
            headers={"User-Agent": "LogiSpim-binutils-builder/2.0"},
        )
        self.log(f"Downloading MSYS2 bootstrap from {MSYS2_INSTALLER_URL}")
        try:
            with urllib.request.urlopen(request, timeout=120) as response, installer.open("wb") as output:
                shutil.copyfileobj(response, output)
            self.run_command(
                [str(installer), "-y", "-oC:\\"],
                description="Installing MSYS2 into C:\\msys64",
            )
        except Exception as exc:
            if isinstance(exc, BuildError):
                raise
            raise BuildError(f"Unable to download or install MSYS2: {exc}") from exc
        finally:
            shutil.rmtree(temporary_directory, ignore_errors=True)
        root = self._find_msys2_root()
        if root is None:
            raise BuildError("MSYS2 installer completed, but C:\\msys64\\usr\\bin\\bash.exe was not found.")
        return root

    def _msys_environment(self, root: Path) -> dict[str, str]:
        """Construct environment overrides selecting MSYS2's native UCRT64 toolchain."""
        environment = os.environ.copy()
        environment["MSYSTEM"] = "UCRT64"
        environment["CHERE_INVOKING"] = "yes"
        environment["MSYS2_PATH_TYPE"] = "inherit"
        environment["MSYS2_ROOT"] = str(root)
        environment["PATH"] = os.pathsep.join(
            [str(root / "ucrt64" / "bin"), str(root / "usr" / "bin"), environment.get("PATH", "")]
        )
        return environment

    def _run_msys(self, root: Path, command: str, *, check: bool = True) -> subprocess.CompletedProcess[str]:
        """Execute a command inside a fresh MSYS2 UCRT64 login shell."""
        bash = root / "usr" / "bin" / "bash.exe"
        return self.run_command(
            [str(bash), "-lc", command],
            env=self._msys_environment(root),
            check=check,
            description=f"MSYS2 UCRT64: {command}",
        )

    def _ucrt64_tools_available(self, root: Path) -> bool:
        """Check that UCRT64 has the compiler, build tools, and GNU makeinfo."""
        required_executables = (
            "gcc.exe",
            "g++.exe",
            "objdump.exe",
        )
        ucrt_bin = root / "ucrt64" / "bin"
        if any(not (ucrt_bin / executable).is_file() for executable in required_executables):
            return False

        # Check makeinfo as a usable Texinfo executable, not merely as a file.
        # This catches partially installed packages and missing Perl/runtime
        # dependencies before configure or make starts.
        result = self._run_msys(
            root,
            "command -v make >/dev/null 2>&1 "
            "&& command -v makeinfo >/dev/null 2>&1 "
            "&& makeinfo --version 2>&1 | grep -qi 'GNU texinfo'",
            check=False,
        )
        return result.returncode == 0

    def _update_msys2(self, root: Path) -> None:
        """Perform the MSYS2 full-system update, reopening the shell if it is replaced."""
        first = self._run_msys(root, "pacman -Syu --noconfirm", check=False)
        if first.returncode != 0:
            self.log("MSYS2 requested a shell restart during its system update; retrying in a fresh shell.")
            second = self._run_msys(root, "pacman -Syu --noconfirm", check=False)
            if second.returncode != 0:
                raise BuildError(f"MSYS2 full-system update failed (exit code {second.returncode}).")

    def _install_ucrt64_packages(self, root: Path) -> None:
        """Install the UCRT64 compiler, MSYS build utilities, and GNU Texinfo."""
        packages = " ".join(MSYS2_PACKAGES)
        self._run_msys(root, f"pacman -S --needed --noconfirm {packages}")

