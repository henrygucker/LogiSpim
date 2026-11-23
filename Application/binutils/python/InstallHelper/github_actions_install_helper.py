"""Non-interactive, runner-aware prerequisite installation for GitHub Actions."""

from __future__ import annotations

import platform
from pathlib import Path

from .install_helper import BuildError, InstallHelper
from .linux_install_helper import LinuxInstallHelper
from .macos_install_helper import MacOSInstallHelper
from .windows_install_helper import WindowsInstallHelper


class GitHubActionsInstallHelper(InstallHelper):
    """Delegate CI prerequisite setup to a runner- and OS-specific child helper.

    This class is deliberately separate from the build helpers. GitHub Actions
    runs the same host-native build implementation as a local machine with the
    same OS and architecture; only dependency installation is CI-aware.
    """

    class _WindowsRunnerInstallHelper(WindowsInstallHelper):
        """Reuse the hosted Windows runner's MSYS2 and install UCRT64 packages only if missing."""

        def install_prerequisites(self) -> dict[str, str]:
            """Install missing Windows runner prerequisites without prompting."""
            return self._install_prerequisites(non_interactive=True)

    class _MacOSRunnerInstallHelper(MacOSInstallHelper):
        """Reuse the runner's architecture-matched Homebrew and Xcode toolchain."""

        def install_prerequisites(self) -> dict[str, str]:
            """Install only missing macOS packages without prompting."""
            return super().install_prerequisites(non_interactive=True)

    class _LinuxRunnerInstallHelper(LinuxInstallHelper):
        """Reuse preinstalled Linux build utilities and install only missing packages."""

        def install_prerequisites(self) -> dict[str, str]:
            """Install only missing Linux packages in non-interactive mode."""
            return super().install_prerequisites(non_interactive=True)

    def __init__(self, binutils_dir: Path, host_tag: str) -> None:
        """Select a GitHub Actions installer for the runner's operating system."""
        self.binutils_dir = binutils_dir
        if host_tag.startswith("windows-"):
            self._platform_helper = self._WindowsRunnerInstallHelper()
        elif host_tag.startswith("macos-"):
            self._platform_helper = self._MacOSRunnerInstallHelper()
        elif host_tag.startswith("linux-"):
            self._platform_helper = self._LinuxRunnerInstallHelper()
        else:
            raise BuildError(f"No GitHub Actions installer exists for host: {host_tag}")

    def install_prerequisites(self) -> dict[str, str]:
        """Install the minimum missing prerequisites for this GitHub Actions runner."""
        if not self.is_github_actions():
            raise BuildError(
                "GitHubActionsInstallHelper was selected outside GitHub Actions; use the local platform installer instead."
            )
        self.log(f"Configuring prerequisites for GitHub Actions runner ({platform.system()} {platform.machine()}).")
        return self._platform_helper.install_prerequisites()
