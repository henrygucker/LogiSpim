Before LogiSpim can be downloaded and installed, it is necessary to install the cross-compilation toolchain
used to compile MIPS source code into executable binaries.

Instructions for varying operating systems are given below.

### MacOS

On MacOS, installation of the MIPS cross compilation toolchain from [this repository](https://github.com/messense/homebrew-macos-cross-toolchains)
is the recommended route.
As is outlined in the linked repository's README, installation through Homebrew (`brew`) is straightforward.

With [Homebrew](https://brew.sh) installed, the `mipsel-unknown-linux-gnu` target toolchain can be installed as follows:
1. Allow Homebrew to access the packages hosted by the repository.
```shell
brew tap messense/macos-cross-toolchains
```
2. Install the cross-compilation toolchain package.
```shell
brew install mipsel-unknown-linux-gnu
```

*Note: Installing precompiled toolchains through the repository's [Github releases](https://github.com/messense/homebrew-macos-cross-toolchains/releases) is
an alternative option.*

### Linux (Debian/Ubuntu)

On Linux, the MIPS cross compilation toolchain provided by [this Debian package](https://packages.debian.org/sid/binutils-mipsel-linux-gnu)
is supported.

Installation can be done as follows:
1. Update the local package index.
```shell
sudo apt update
```
2. Install the cross-compilation toolchain package.
```shell
sudo apt install binutils-mipsel-linux-gnu
```

### Windows

Due to a lack of native cross-compilation toolchains for Windows, Windows Subsystem for Linux (WSL) is used.
Steps for installation on Windows are as follows:
1. Follow [this guide](https://learn.microsoft.com/en-us/windows/wsl/install) for installing WSL.
   * If you already have WSL installed, ensure that a Debian-based distribution (such as Ubuntu) is installed
2. From a PowerShell instance opened as Administrator, run the following command to update WSL's local package index.
```shell
wsl sudo apt update
```
3. From this same PowerShell instance, run the following command to install the cross-compilation toolchain package on the WSL instance.
```shell
sudo apt install binutils-mipsel-linux-gnu
```
