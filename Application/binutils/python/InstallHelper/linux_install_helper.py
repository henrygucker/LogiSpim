"""Linux prerequisite installers for Debian, Fedora, Arch, and SUSE systems."""

from __future__ import annotations

import os
import shutil
from dataclasses import dataclass

from .install_helper import BuildError, InstallHelper


@dataclass(frozen=True)
class _LinuxPackageStrategy:
    """Describe one Linux package-manager command and its required packages."""

    name: str
    packages: tuple[str, ...]
    update_command: tuple[str, ...]
    install_command: tuple[str, ...]


class _LinuxDistributionInstallHelper:
    """Shared implementation for Linux distribution-specific package managers."""

    strategy: _LinuxPackageStrategy
    required_commands = ("gcc", "g++", "make", "bash", "tar", "xz")

    def install_prerequisites(self, *, non_interactive: bool = False) -> dict[str, str]:
        """Install missing native compiler and build utilities using this distribution's manager."""
        missing = [command for command in self.required_commands if not shutil.which(command)]
        if missing:
            package_list = ", ".join(self.strategy.packages)
            if not InstallHelper.approve_installation(
                [*missing, f"{self.strategy.name} packages: {package_list}"],
                non_interactive=non_interactive,
            ):
                raise BuildError("Linux dependency installation was declined; no build was performed.")
            self._install_packages(non_interactive=non_interactive)

        still_missing = [command for command in self.required_commands if not shutil.which(command)]
        if still_missing:
            raise BuildError(
                "Linux prerequisites remain unavailable after installation: " + ", ".join(still_missing)
            )
        return os.environ.copy()

    def _install_packages(self, *, non_interactive: bool) -> None:
        """Run package-manager update and install commands with suitable privileges."""
        prefix = self._privilege_prefix(non_interactive=non_interactive)
        environment = os.environ.copy()
        if self.strategy.name == "apt":
            environment["DEBIAN_FRONTEND"] = "noninteractive"
        if self.strategy.update_command:
            InstallHelper.run_command(
                [*prefix, *self.strategy.update_command],
                env=environment,
                description=f"Refreshing {self.strategy.name} package metadata",
            )
        command = [*prefix, *self.strategy.install_command, *self.strategy.packages]
        InstallHelper.run_command(
            command,
            env=environment,
            description=f"Installing Linux build dependencies with {self.strategy.name}",
        )

    def _privilege_prefix(self, *, non_interactive: bool) -> list[str]:
        """Return the privilege prefix, obtaining sudo authorization when required."""
        if InstallHelper.is_administrator():
            return []
        sudo = shutil.which("sudo")
        if not sudo:
            raise BuildError(
                "Administrator privileges are required to install Linux packages, but sudo is unavailable."
            )
        if non_interactive or InstallHelper.is_github_actions():
            return [sudo, "-n"]
        InstallHelper.run_command([sudo, "-v"], description="Requesting administrator privileges for package installation")
        return [sudo]


class _DebianInstallHelper(_LinuxDistributionInstallHelper):
    """Install prerequisites on Debian and Ubuntu using apt."""

    strategy = _LinuxPackageStrategy(
        name="apt",
        packages=("build-essential", "bash", "xz-utils"),
        update_command=("apt-get", "update"),
        install_command=("apt-get", "install", "-y"),
    )


class _FedoraInstallHelper(_LinuxDistributionInstallHelper):
    """Install prerequisites on Fedora and RHEL-family systems using dnf or yum."""

    def __init__(self, manager: str) -> None:
        """Initialize the Fedora-family installer with the detected package manager."""
        if manager == "dnf":
            self.strategy = _LinuxPackageStrategy(
                name="dnf",
                packages=("gcc", "gcc-c++", "make", "bash", "xz"),
                update_command=(),
                install_command=("dnf", "install", "-y"),
            )
        else:
            self.strategy = _LinuxPackageStrategy(
                name="yum",
                packages=("gcc", "gcc-c++", "make", "bash", "xz"),
                update_command=(),
                install_command=("yum", "install", "-y"),
            )


class _ArchInstallHelper(_LinuxDistributionInstallHelper):
    """Install prerequisites on Arch Linux using pacman."""

    strategy = _LinuxPackageStrategy(
        name="pacman",
        packages=("base-devel", "bash", "xz"),
        update_command=("pacman", "-Syu", "--noconfirm"),
        install_command=("pacman", "-S", "--needed", "--noconfirm"),
    )


class _SuseInstallHelper(_LinuxDistributionInstallHelper):
    """Install prerequisites on openSUSE and SUSE using zypper."""

    strategy = _LinuxPackageStrategy(
        name="zypper",
        packages=("gcc", "gcc-c++", "make", "bash", "xz"),
        update_command=(),
        install_command=("zypper", "--non-interactive", "install", "--no-recommends"),
    )


class LinuxInstallHelper(InstallHelper):
    """Select an apt, Fedora-family, Arch, or SUSE prerequisite installer."""

    def __init__(self) -> None:
        """Detect the available Linux package manager and create its strategy helper."""
        if shutil.which("apt-get"):
            self._distribution_helper = _DebianInstallHelper()
        elif shutil.which("dnf"):
            self._distribution_helper = _FedoraInstallHelper("dnf")
        elif shutil.which("yum"):
            self._distribution_helper = _FedoraInstallHelper("yum")
        elif shutil.which("pacman"):
            self._distribution_helper = _ArchInstallHelper()
        elif shutil.which("zypper"):
            self._distribution_helper = _SuseInstallHelper()
        else:
            raise BuildError("No supported Linux package manager found (apt, dnf, yum, pacman, or zypper).")

    def install_prerequisites(self, *, non_interactive: bool = False) -> dict[str, str]:
        """Install only missing dependencies through the detected distribution helper."""
        return self._distribution_helper.install_prerequisites(non_interactive=non_interactive)
