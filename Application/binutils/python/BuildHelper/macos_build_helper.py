"""Host-native macOS build helper for Intel and Apple Silicon machines."""

from pathlib import Path

from .build_helper import PosixBuildHelper
from ..InstallHelper.install_helper import InstallHelper


class MacOSBuildHelper(PosixBuildHelper):
    """Build host-native macOS Mach-O binutils while targeting mipsel-none-elf."""

    def __init__(self, binutils_dir: Path, install_helper: InstallHelper, host_tag: str) -> None:
        """Initialize the shared POSIX build flow for the selected macOS architecture."""
        super().__init__(binutils_dir, install_helper, host_tag)
