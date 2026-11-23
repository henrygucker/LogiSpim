# GNU Binutils Builder

## Summary

This directory serves to house a locally compiled set of GNU binutils for the LogiSpim application to use.
These enable compilation and disassembly of MIPS assembly on any machine.

Automating the source downloading, prerequisite installation, and building is done through a python script, `build.py`.
Read the following sections to use it prior to running the LogiSpim application from a development environment (or
packaging the application).

Note that the resulting binutils are bundled with the LogiSpim application to simplify user installation of LogiSpim.

### Prerequisites
 - Python 3.10 or newer

## Quick Start

Simply run the `build.py` script.

From the repository root:
```bash
python path/to/build.py
```

### Note
If you receive a `urlopen` error citing a failure to get the local issuer certificate, then use pip (install it if not already installed)
to run the following command:
```shell
pip install pip-system-certs
```

## Build Script Details

This information is not necessary for using the script, but does provide some insight into its implementation and
functionality.

### Build Configuration

The Bash build script configures GNU Binutils with the following flags:

```text
--target=mipsel-none-elf
--disable-nls
--disable-werror
--disable-gdb
--disable-sim
--disable-gprofng
--without-zstd
--enable-deterministic-archives
```

### Local runs

When prerequisites are missing, the script prints the missing dependencies and asks whether it should install them (`y/n`). Answering `n` exits without building. Answering `y` allows the platform installer to request administrative authorization as needed. If the process already has administrator/root privileges, it logs that it can proceed without asking again.

- **macOS:** Homebrew is the package manager. If Homebrew is absent, the script asks before running Homebrew's official installer. Homebrew is always installed and run as the normal user, never as root. If invoked with `sudo`, `build.py` returns the build process to `SUDO_USER`. Intel Homebrew is normally under `/usr/local`; Apple Silicon Homebrew is normally under `/opt/homebrew`. Xcode Command Line Tools provide Apple's SDK and Clang. Homebrew's `make` formula supplies GNU Make as `gmake` when needed.
- **Windows:** The script reuses MSYS2 if found (including `C:\msys64`). If MSYS2 is absent, it asks before downloading the official MSYS2 bootstrap installer. Windows UAC is used when administrator access is required. It installs `base-devel`, `mingw-w64-ucrt-x86_64-gcc`, and `mingw-w64-ucrt-x86_64-binutils` when UCRT64 tools are missing.
- **Linux:** The script supports apt-based Debian/Ubuntu, dnf/yum-based Fedora/RHEL-family, Arch/pacman, and SUSE/zypper systems. It installs the native C/C++ compiler, GNU Make, Bash, and xz utilities when missing. Package-manager operations use `sudo` when the process is not root.

### GitHub Actions

When `GITHUB_ACTIONS=true`, no prompt is displayed. The CI installer checks the current runner and installs only missing packages. If a runner image changes and dependency installation cannot proceed, the build exits with a diagnostic rather than waiting for input.

The current GitHub-hosted runner labels matching the four supported builds are:

| Workflow label   | Host architecture | Intended artifact   |
|------------------|-------------------|---------------------|
| `windows-latest` | Windows x86-64    | Windows x86-64      |
| `macos-latest`   | macOS arm64       | macOS Apple Silicon |
| `macos-26-intel` | macOS x86-64      | macOS Intel         |
| `ubuntu-latest`  | Linux x86-64      | Linux x86-64        |

## Information for Future Work

### Code Organization

The code is separated into build orchestration, host build behavior, and prerequisite installation.

#### Build Helpers

`BuildHelper` in `python/BuildHelper/build_helper.py` is the shared build orchestrator. It reads the pinned version and checksum, downloads and verifies the source archive, prepares directories, invokes the build script, and verifies the installed tools.
It inherits from `InstallHelper` and receives a platform installer through its constructor.

Operations that cannot be implemented portably are explicit extension points in `BuildHelper`, and concrete build
helpers implement those operations:

| Class                | Module                                       | Host-specific behavior                                                                    |
|----------------------|----------------------------------------------|-------------------------------------------------------------------------------------------|
| `WindowsBuildHelper` | `python/buildHelper/windows_build_helper.py` | Extracts with MSYS2 tar, runs the script in UCRT64 Bash, and bundles required MinGW DLLs. |
| `MacOSBuildHelper`   | `python/buildHelper/macos_build_helper.py`   | Uses native Bash/tar and restores POSIX executable permissions.                           |
| `LinuxBuildHelper`   | `python/buildHelper/linux_build_helper.py`   | Uses native Bash/tar and restores POSIX executable permissions.                           |

#### Prerequisite installers

`InstallHelper` in `python/installHelper/install_helper.py` defines the common installer interface and shared
process, logging, privilege, and confirmation utilities.
Its `install_prerequisites()` method raises `NotImplementedError` since it is overwritten by the concrete,
platform-specific `...InstallHelper` classes.

| Class                        | Module                                                  | Responsibility                                                                                                                  |
|------------------------------|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `WindowsInstallHelper`       | `python/installHelper/windows_install_helper.py`        | Finds or bootstraps MSYS2, updates it when UCRT64 tools are absent, and installs the UCRT64 compiler/build packages.            |
| `MacOSInstallHelper`         | `python/installHelper/macos_install_helper.py`          | Selects an internal Intel or Apple Silicon strategy, installs Homebrew if absent, and installs missing Clang/GNU Make packages. |
| `LinuxInstallHelper`         | `python/installHelper/linux_install_helper.py`          | Selects an internal Debian/apt, Fedora-family/dnf-or-yum, Arch/pacman, or SUSE/zypper strategy.                                 |
| `GitHubActionsInstallHelper` | `python/installHelper/github_actions_install_helper.py` | Selects a CI-specific child installer for Windows, macOS, or Linux and runs it without prompts.                                 |

The GitHub Actions installer delegates to OS-specific child classes that reuse the local platform installers' setup logic in non-interactive mode. It checks for installed tools first and avoids updating/installing packages when the necessary toolchain is already available.

### Changing the GNU Binutils version

The release version and expected source digest are intentionally stored outside the code:

1. Change `.gnu_version` to the desired GNU Binutils release version.
2. Change `.gnu_sha256` to the SHA-256 digest of the matching official `binutils-VERSION.tar.xz` archive.
3. Obtain the digest from GNU's official release announcement or another GNU-maintained source. Do not disable checksum verification.

The checked-in values pin GNU Binutils **2.47** and the SHA-256 digest for `binutils-2.47.tar.xz`. The script refuses to continue if the archive does not match `.gnu_sha256`.
