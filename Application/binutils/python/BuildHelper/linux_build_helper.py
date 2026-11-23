"""Host-native Linux build helper for x86-64 Linux distributions."""

from pathlib import Path

from .build_helper import PosixBuildHelper
from ..InstallHelper.install_helper import InstallHelper


class LinuxBuildHelper(PosixBuildHelper):
    """Build host-native Linux ELF binutils while targeting mipsel-none-elf."""

    def __init__(self, binutils_dir: Path, install_helper: InstallHelper, host_tag: str) -> None:
        """Initialize the shared POSIX build flow for the selected Linux host."""
        super().__init__(binutils_dir, install_helper, host_tag)
