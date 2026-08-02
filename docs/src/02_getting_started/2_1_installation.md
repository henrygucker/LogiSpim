> [!WARNING]
> This guide assumes you have Java 22+ installed on your device, as well as Logisim-evolution:
> - Java 22 can be downloaded [here](https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html).
> - Logisim-evolution can be downloaded from the [Logisim-evolution GitHub repository](https://github.com/logisim-evolution/logisim-evolution).

## Prerequisite Installation Steps
Before LogiSpim can be downloaded and installed, it is necessary to install the cross-compilation toolchain
used to compile MIPS source code into executable binaries. 
Instructions for varying operating systems are given below.

### MacOS

On MacOS, installation of the MIPS cross compilation toolchain from [this repository](https://github.com/messense/homebrew-macos-cross-toolchains)
is supported by the file processor.
As is outlined in the linked repository's README, installation through [Homebrew](https://brew.sh) (`brew`) is straightforward.

With [Homebrew](https://brew.sh) installed, the `mipsel-unknown-linux-gnu` target toolchain can be installed as follows:
1. Allow Homebrew to access the packages hosted by the repository.
```shell
brew tap messense/macos-cross-toolchains
```
2. Install the cross-compilation toolchain package.
```shell
brew install mipsel-unknown-linux-gnu
```

> [!NOTE]
> *Installing precompiled toolchains through the repository's [Github releases](https://github.com/messense/homebrew-macos-cross-toolchains/releases) is
> an alternative option.*

### Linux (Debian/Ubuntu)

On Linux, the MIPS cross compilation toolchain provided by [this Debian package](https://packages.debian.org/sid/binutils-mipsel-linux-gnu)
is supported by the file processor.

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

Due to a lack of native cross-compilation toolchains for Windows, Windows Subsystem for Linux (WSL) must be used.

Steps for installation on Windows are as follows:
1. Follow [this guide](https://learn.microsoft.com/en-us/windows/wsl/install) for installing WSL.
   * If you already have WSL installed, ensure that a Debian-based distribution (such as Ubuntu) is installed
2. From a PowerShell instance opened as Administrator, run the following command to update WSL's local package index.
```shell
wsl sudo apt update
```
3. From this same PowerShell instance, run the following command to install the cross-compilation toolchain package on the WSL instance.
```shell
wsl sudo apt install binutils-mipsel-linux-gnu
```

___

## LogiSpim Installation
> [!CAUTION]
> This page will be completed once GitHub Releases page is completed.
> For the time being, build and run the File Processor with [Maven](https://maven.apache.org).
> The Logisim-Evolution circuit can be found at the root directory of the repository.
