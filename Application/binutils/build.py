#!/usr/bin/env python3
"""Cross-platform entry point for building host-native GNU binutils for mipsel-none-elf."""

from __future__ import annotations

import argparse
import os
import platform
import shutil
import subprocess
import sys
from pathlib import Path

from python.BuildHelper.build_helper import BuildHelper
from python.InstallHelper.github_actions_install_helper import GitHubActionsInstallHelper
from python.InstallHelper.install_helper import BuildError, InstallHelper
from python.BuildHelper.linux_build_helper import LinuxBuildHelper
from python.InstallHelper.linux_install_helper import LinuxInstallHelper
from python.BuildHelper.macos_build_helper import MacOSBuildHelper
from python.InstallHelper.macos_install_helper import MacOSInstallHelper
from python.BuildHelper.windows_build_helper import WindowsBuildHelper
from python.InstallHelper.windows_install_helper import WindowsInstallHelper


BINUTILS_DIR = Path(__file__).resolve().parent


def detect_host() -> str:
    """Return the supported OS/architecture tag for the current Python process."""
    system = platform.system().lower()
    machine = platform.machine().lower()
    if system == "darwin":
        if machine in {"x86_64", "amd64"}:
            return "macos-x86_64"
        if machine in {"arm64", "aarch64"}:
            return "macos-aarch64"
        raise BuildError(f"Unsupported macOS architecture: {machine}")
    if system == "windows":
        if machine in {"x86_64", "amd64"}:
            return "windows-x86_64"
        raise BuildError(f"Unsupported Windows architecture: {machine}")
    if system == "linux":
        if machine in {"x86_64", "amd64"}:
            return "linux-x86_64"
        raise BuildError(f"Unsupported Linux architecture: {machine}")
    raise BuildError(f"Unsupported build operating system: {system}")


def create_install_helper(host_tag: str, *, elevated_relaunch: bool) -> InstallHelper:
    """Create the local or GitHub Actions prerequisite installer for the detected host."""
    if InstallHelper.is_github_actions():
        return GitHubActionsInstallHelper(BINUTILS_DIR, host_tag)
    if host_tag.startswith("windows-"):
        return WindowsInstallHelper(elevated_relaunch=elevated_relaunch)
    if host_tag.startswith("macos-"):
        return MacOSInstallHelper()
    if host_tag.startswith("linux-"):
        return LinuxInstallHelper()
    raise BuildError(f"No prerequisite installer is available for host: {host_tag}")


def create_build_helper(host_tag: str, install_helper: InstallHelper) -> BuildHelper:
    """Create the OS-specific build strategy, independent of local versus CI execution."""
    if host_tag.startswith("windows-"):
        return WindowsBuildHelper(BINUTILS_DIR, install_helper, host_tag)
    if host_tag.startswith("macos-"):
        return MacOSBuildHelper(BINUTILS_DIR, install_helper, host_tag)
    if host_tag.startswith("linux-"):
        return LinuxBuildHelper(BINUTILS_DIR, install_helper, host_tag)
    raise BuildError(f"No build helper is available for host: {host_tag}")


def return_to_macos_invoking_user() -> int | None:
    """Avoid running Homebrew as root when build.py was started with sudo on macOS."""
    if platform.system() != "Darwin" or not hasattr(os, "geteuid") or os.geteuid() != 0:
        return None
    original_user = os.environ.get("SUDO_USER")
    if not original_user:
        raise BuildError(
            "Homebrew must not be run as root. Start build.py as your normal macOS user; "
            "Homebrew will request administrator authorization if needed."
        )
    sudo = shutil.which("sudo")
    if not sudo:
        raise BuildError("Cannot resume as the original macOS user because sudo was not found.")
    script = Path(__file__).resolve()
    command = [
        sudo,
        "-u",
        original_user,
        "-H",
        "/usr/bin/env",
        "BINUTILS_ADMIN_APPROVED=1",
        sys.executable,
        str(script),
    ]
    command.extend(argument for argument in sys.argv[1:] if argument != "--elevated-relaunch")
    InstallHelper.log(f"Continuing as the original macOS user ({original_user}) so Homebrew is not run as root.")
    result = subprocess.run(command, check=False)
    return result.returncode


def parse_arguments() -> argparse.Namespace:
    """Parse internal command-line flags used when Windows UAC restarts the build."""
    parser = argparse.ArgumentParser(
        description="Download, build, and install host-native GNU binutils for mipsel-none-elf."
    )
    parser.add_argument(
        "--elevated-relaunch",
        action="store_true",
        help=argparse.SUPPRESS,
    )
    return parser.parse_args()


def main() -> int:
    """Select helpers, execute the build pipeline, and report user-facing failures."""
    arguments = parse_arguments()
    try:
        original_user_exit_code = return_to_macos_invoking_user()
        if original_user_exit_code is not None:
            return original_user_exit_code

        host_tag = detect_host()
        InstallHelper.log(f"Detected build host: {host_tag}")

        if InstallHelper.is_github_actions():
            InstallHelper.log("GitHub Actions detected; dependency setup will be non-interactive.")

        installer = create_install_helper(host_tag, elevated_relaunch=arguments.elevated_relaunch)
        build_helper = create_build_helper(host_tag, installer)

        build_helper.build()

        return 0
    except BuildError as exc:
        print(f"[binutils] ERROR: {exc}", file=sys.stderr, flush=True)
        return 1


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("[binutils] ERROR: Build interrupted by user.", file=sys.stderr, flush=True)
        raise SystemExit(130)
