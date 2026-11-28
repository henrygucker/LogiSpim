"""Homebrew-based prerequisite installation for Intel and Apple Silicon macOS."""

from __future__ import annotations

import os
import platform
import shutil
import tempfile
import urllib.request
from pathlib import Path

from .install_helper import BuildError, InstallHelper


HOMEBREW_INSTALL_URL = "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh"


class _MacOSArchitectureInstallHelper:
    """Shared Homebrew installation implementation for a macOS architecture."""

    brew_candidates: tuple[str, ...] = ()

    def __init__(self) -> None:
        """Initialize the architecture-specific Homebrew installer."""
        self._environment = os.environ.copy()

    def install_prerequisites(self, *, non_interactive: bool = False) -> dict[str, str]:
        """Ensure Homebrew, Clang, and GNU Make are available, then return build variables."""
        brew = self._find_brew()
        if not brew:
            missing = ["Homebrew (macOS package manager)"]
            if not InstallHelper.approve_installation(missing, non_interactive=non_interactive):
                raise BuildError("Homebrew installation was declined; no build was performed.")
            brew = self._install_homebrew(non_interactive=non_interactive)

        self._environment["PATH"] = str(Path(brew).parent) + os.pathsep + self._environment.get("PATH", "")
        self._environment["HOMEBREW_NO_AUTO_UPDATE"] = "1"

        missing_commands: list[str] = []
        if not shutil.which("clang", path=self._environment["PATH"]):
            missing_commands.append("Clang C compiler")
        if not shutil.which("clang++", path=self._environment["PATH"]):
            missing_commands.append("Clang C++ compiler")
        if not shutil.which("gmake", path=self._environment["PATH"]):
            missing_commands.append("GNU Make (gmake)")
        if not shutil.which("makeinfo", path=self._environment["PATH"]):
            missing_commands.append("GNU Texinfo (makeinfo)")

        if missing_commands:
            if not InstallHelper.approve_installation(missing_commands, non_interactive=non_interactive):
                raise BuildError("macOS build dependencies were not installed; no build was performed.")
            packages: list[str] = []
            if "Clang C compiler" in missing_commands or "Clang C++ compiler" in missing_commands:
                packages.append("llvm")
            if "GNU Make (gmake)" in missing_commands:
                packages.append("make")
            if "GNU Texinfo (makeinfo)" in missing_commands:
                packages.append("texinfo")
            InstallHelper.run_command(
                [brew, "install", *packages],
                env=self._environment,
                description=f"Installing missing macOS dependencies with Homebrew: {', '.join(packages)}",
            )

        llvm_prefix = self._brew_prefix(brew, "llvm")
        if llvm_prefix and (llvm_prefix / "bin" / "clang").is_file():
            self._environment["CC"] = str(llvm_prefix / "bin" / "clang")
            self._environment["CXX"] = str(llvm_prefix / "bin" / "clang++")
            self._environment["PATH"] = str(llvm_prefix / "bin") + os.pathsep + self._environment["PATH"]

        texinfo_prefix = self._brew_prefix(brew, "texinfo")
        if texinfo_prefix and (texinfo_prefix / "bin").is_dir():
            self._environment["PATH"] = str(texinfo_prefix / "bin") + os.pathsep + self._environment["PATH"]
        InstallHelper.require_command("makeinfo", path=self._environment["PATH"])

        make_prefix = self._brew_prefix(brew, "make")
        if make_prefix and (make_prefix / "bin" / "gmake").is_file():
            self._environment["MAKE"] = str(make_prefix / "bin" / "gmake")
        else:
            self._environment["MAKE"] = InstallHelper.require_command("gmake", path=self._environment["PATH"])

        self._environment["CC"] = self._environment.get("CC") or InstallHelper.require_command(
            "clang", path=self._environment["PATH"]
        )
        self._environment["CXX"] = self._environment.get("CXX") or InstallHelper.require_command(
            "clang++", path=self._environment["PATH"]
        )
        return self._environment

    def _find_brew(self) -> str | None:
        """Find Homebrew, preferring the conventional prefix for this architecture."""
        candidates = [*self.brew_candidates, shutil.which("brew")]
        for candidate in candidates:
            if candidate and Path(candidate).is_file():
                return str(Path(candidate).absolute())
        return None

    def _install_homebrew(self, *, non_interactive: bool) -> str:
        """Download and execute Homebrew's official installer as the normal user."""
        if InstallHelper.is_administrator():
            raise BuildError(
                "Homebrew must not be installed or run as root. Re-run build.py as the normal macOS user."
            )
        installer = Path(tempfile.gettempdir()) / "binutils-homebrew-install.sh"
        request = urllib.request.Request(
            HOMEBREW_INSTALL_URL,
            headers={"User-Agent": "LogiSpim-binutils-builder/2.0"},
        )
        InstallHelper.log("Downloading the official Homebrew installer.")
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                installer.write_bytes(response.read())
            environment = self._environment.copy()
            if non_interactive or InstallHelper.is_github_actions():
                environment["NONINTERACTIVE"] = "1"
            InstallHelper.run_command(
                ["/bin/bash", str(installer)],
                env=environment,
                description="Installing Homebrew as the current non-root user",
            )
        except Exception as exc:
            if isinstance(exc, BuildError):
                raise
            raise BuildError(f"Unable to install Homebrew: {exc}") from exc
        finally:
            installer.unlink(missing_ok=True)
        brew = self._find_brew()
        if not brew:
            raise BuildError("Homebrew installation completed, but the brew executable could not be located.")
        return brew

    def _brew_prefix(self, brew: str, formula: str) -> Path | None:
        """Return the installation prefix for a Homebrew formula, if installed."""
        result = InstallHelper.run_command(
            [brew, "--prefix", formula],
            env=self._environment,
            check=False,
            capture_output=True,
            description=f"Locating Homebrew formula: {formula}",
        )
        if result.returncode != 0 or not result.stdout.strip():
            return None
        return Path(result.stdout.strip())


class _IntelMacOSInstallHelper(_MacOSArchitectureInstallHelper):
    """Install macOS build prerequisites using the Intel Homebrew prefix."""

    brew_candidates = ("/usr/local/bin/brew",)


class _AppleSiliconMacOSInstallHelper(_MacOSArchitectureInstallHelper):
    """Install macOS build prerequisites using the Apple Silicon Homebrew prefix."""

    brew_candidates = ("/opt/homebrew/bin/brew",)


class MacOSInstallHelper(InstallHelper):
    """Select the Intel or Apple Silicon Homebrew installer for the current Mac."""

    def __init__(self) -> None:
        """Select the internal installer from the current macOS machine architecture."""
        machine = platform.machine().lower()
        if machine in {"x86_64", "amd64"}:
            self._architecture_helper = _IntelMacOSInstallHelper()
        elif machine in {"arm64", "aarch64"}:
            self._architecture_helper = _AppleSiliconMacOSInstallHelper()
        else:
            raise BuildError(f"Unsupported macOS architecture: {machine}")

    def install_prerequisites(self, *, non_interactive: bool = False) -> dict[str, str]:
        """Install only missing prerequisites using the selected Homebrew strategy."""
        return self._architecture_helper.install_prerequisites(non_interactive=non_interactive)
