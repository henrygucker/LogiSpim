"""Shared installation utilities and the prerequisite-installer interface."""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Mapping, Sequence


class BuildError(RuntimeError):
    """Raised when a prerequisite, download, build, or verification step fails."""


class InstallHelper:
    """Base class for installing native build prerequisites.

    Platform-specific subclasses implement :meth:`install_prerequisites` and
    return environment-variable overrides that the build process should use.
    The base implementation deliberately raises ``NotImplementedError`` so a
    new platform cannot silently proceed without an installation strategy.
    """

    def install_prerequisites(self) -> dict[str, str]:
        """Install all prerequisites required by the selected host platform.

        Returns:
            A mapping of environment-variable overrides for subsequent build
            commands.

        Raises:
            NotImplementedError: Always; subclasses must provide the platform
                implementation.
        """
        raise NotImplementedError(
            f"{type(self).__name__}.install_prerequisites() is not implemented."
        )

    @staticmethod
    def log(message: str) -> None:
        """Print a consistently prefixed, immediately flushed log message."""
        print(f"[binutils] {message}", flush=True)

    @staticmethod
    def run_command(
        command: Sequence[str | os.PathLike[str]],
        *,
        env: Mapping[str, str] | None = None,
        cwd: Path | None = None,
        check: bool = True,
        capture_output: bool = False,
        description: str | None = None,
    ) -> subprocess.CompletedProcess[str]:
        """Run a subprocess and convert launch or nonzero-exit failures to BuildError."""
        arguments = [os.fspath(part) for part in command]
        label = description or " ".join(arguments)
        InstallHelper.log(label)
        try:
            result = subprocess.run(
                arguments,
                cwd=str(cwd) if cwd else None,
                env=dict(env) if env is not None else None,
                text=True,
                capture_output=capture_output,
                check=False,
            )
        except FileNotFoundError as exc:
            raise BuildError(f"Required command was not found: {arguments[0]} ({exc})") from exc
        if check and result.returncode != 0:
            detail = result.stderr.strip() if result.stderr else ""
            suffix = f"\n{detail}" if detail else ""
            raise BuildError(f"Command failed with exit code {result.returncode}: {label}{suffix}")
        return result

    @staticmethod
    def is_github_actions() -> bool:
        """Return whether this process is running inside a GitHub Actions job."""
        return os.environ.get("GITHUB_ACTIONS", "").strip().lower() == "true"

    @staticmethod
    def is_administrator() -> bool:
        """Return whether the current process has administrator/root privileges."""
        if os.name == "nt":
            try:
                import ctypes

                return bool(ctypes.windll.shell32.IsUserAnAdmin())
            except Exception:
                return False
        return hasattr(os, "geteuid") and os.geteuid() == 0

    @classmethod
    def approve_installation(cls, missing: Sequence[str], *, non_interactive: bool = False) -> bool:
        """Report missing dependencies and ask before a local installation.

        CI jobs never prompt. Locally, users already running with administrative
        privileges are not asked a redundant question; otherwise a ``y/n``
        confirmation is required before the platform helper requests elevation.
        """
        if not missing:
            return True
        cls.log("The following build dependencies are missing:")
        for dependency in missing:
            print(f"  - {dependency}", flush=True)
        if non_interactive or cls.is_github_actions():
            cls.log("Non-interactive mode: proceeding with dependency installation.")
            return True
        if os.environ.get("BINUTILS_ADMIN_APPROVED") == "1":
            cls.log("Dependency installation was approved by the elevated/restarted build process.")
            return True
        if cls.is_administrator():
            cls.log("Administrator privileges are already available; proceeding with dependency installation.")
            return True
        if not sys.stdin.isatty():
            cls.log("No interactive terminal is available to request installation approval.")
            return False
        try:
            answer = input("Install these dependencies now? [y/N] ").strip().lower()
        except EOFError:
            return False
        return answer in {"y", "yes"}

    @staticmethod
    def require_command(command: str, *, path: str | None = None) -> str:
        """Resolve a required executable or raise an actionable BuildError."""
        resolved = shutil.which(command, path=path)
        if not resolved:
            raise BuildError(f"Required executable is still unavailable after setup: {command}")
        return resolved
