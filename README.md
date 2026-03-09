# Installation (MacOS w/Homebrew)

## 1.
In a terminal, run the following command to add the cross-toolchains tap:
```
brew tap messense/macos-cross-toolchains
```

## 2.
Then, run this command to install the cross-compilation toolchain for MIPS:
```
mipsel-unknown-linux-gnu
```

After these steps have been completed, MIPS files ending in .s can be converted into binary files for the processor in logisim.

