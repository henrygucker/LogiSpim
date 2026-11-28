# LogiSpim Installation Guide for Linux (Debian/Ubuntu)

## 1. Install Logisim-evolution (v5.0.0+)
To function correctly, LogiSpim requires Logisim-evolution v5.0.0+ to be installed as an application
(not just a downloaded JAR).

To install Logisim-evolution, download the most recent `..._amd64.deb` package from
the [releases page](https://github.com/logisim-evolution/logisim-evolution/releases).

For your convenience, [this](https://github.com/logisim-evolution/logisim-evolution/releases/download/v5.0.0/logisim-evolution_5.0.0_amd64.deb)
is a direct link to download the v5.0.0 package.

Then, run the following command to install Logisim-evolution:
```shell
sudo apt install ./path/to/package.deb
```

## 2. Install LogiSpim

To install LogiSpim, download the most recent `...-linux-x86_64.deb` package from
LogiSpim's [releases page](https://github.com/henrygucker/LogiSpim/releases).

Finally, install the package with a similar shell command as used above.

## 3. Installation Complete
Your installation is now complete, and you should be able to launch the LogiSpim app!

Move on to the [usage guide](../../04_usage_guide/index.md) to run your first program on the LogiSpim
processor!
