<div align="center">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="./assets/logo/LogiSpim_Logo_Light.svg">
      <source media="(prefers-color-scheme: light)" srcset="./assets/logo/LogiSpim_Logo_Dark.svg">
      <img alt="LogiSpim Logo" width="500" src="./assets/logo/LogiSpim_Logo_Both_Invert.svg">
    </picture>

[![Latest GitHub Release](https://img.shields.io/github/v/release/henrygucker/LogiSpim?include_prereleases&sort=semver&display_name=tag&style=flat&label=latest)](https://github.com/henrygucker/LogiSpim/releases)
[![Maven CI Status Shield](https://img.shields.io/github/actions/workflow/status/henrygucker/LogiSpim/maven.yml?style=flat&label=CI&branch=main)](https://github.com/henrygucker/LogiSpim/actions/workflows/maven.yml)
[![Docs Build Status Shield](https://img.shields.io/github/actions/workflow/status/henrygucker/LogiSpim/mdbook.yml?style=flat&branch=main&label=docs)](https://henrygucker.github.io/LogiSpim/)
[![GitHub License](https://img.shields.io/github/license/henrygucker/LogiSpim?label=license)](LICENSE)

[Website][Documentation Page] | [Overview][Get Started] | [Installation Guide] | [Usage Guide] | [FAQ][FAQ Page]
</div>

[Documentation Page]: https://henrygucker.github.io/LogiSpim/01_intro/index.html
[Get Started]: https://henrygucker.github.io/LogiSpim/02_what_is_logispim/index.html
[Installation Guide]: https://henrygucker.github.io/LogiSpim/03_installation/index.html
[Usage Guide]: https://henrygucker.github.io/LogiSpim/04_usage_guide/index.html
[FAQ Page]: https://henrygucker.github.io/LogiSpim/05_faq/index.html

## Summary

**LogiSpim** interactively teaches pipelined MIPS microarchitecture through exploration of its pipelined 32-bit MIPS
processor built in [Logisim-evolution](https://github.com/logisim-evolution/logisim-evolution).
By running their own MIPS programs on the processor, students engage with the digital logic performing its
instructions in every step.

## Processor Features
* User-friendly clock and reset controls supporting step, auto-tick, and reset operations with keybinds
* Visual displays of pipeline state over time, depicting instruction motion through pipeline
* Syscall support for printing and exiting from programs through a lightweight kernel
* Built-in breakpoints for debugging
* Displays current register values and program assembly within Logisim-evolution

### Feature Showcase
* <details>
    <summary>Printing & Register Viewer</summary>

Printing to the emulated console through syscalls with the register view shown on the left:

<img alt="Printing Demo" width="700" src="docs/src/01_intro/assets/printing_demo.gif">
</details>

* <details>
    <summary>Clock Stepping & Assembly Viewer</summary>

Manually stepping through instructions while zoomed into the processor's execute stage with the Assembly Viewer open:

<img alt="Stepping through instructions demo" width="700" src="docs/src/01_intro/assets/execute_stage_demo.gif">
</details>

## LogiSpim Utility Application
The LogiSpim application enables users to run their own programs on LogiSpim's processor through some
utilities:
* Automated compilation and processing of source code
* Disassembly view with interleaved source code
* Integrated Logisim-evolution controls and circuit management

More information about the application and its role in the LogiSpim project, 
look [here](https://henrygucker.github.io/LogiSpim/02_what_is_logispim/index.html#logispims-novelty).

### Showcase
* <details>
    <summary>Compilation with Disassembly View</summary>

Compiling a MIPS program and viewing the disassembly, while processed binary files prepare for import to Logisim-evolution:

<img alt="Stepping through instructions demo" width="700" loading="lazy" decoding="async" src="docs/src/01_intro/assets/demo_compile.gif">
</details>

* <details>
    <summary>LogiSpim Application Workflow</summary>

To streamline the compilation and loading process, the LogiSpim application compiles MIPS source code into files which can be easily
imported into the LogiSpim processor.

The described workflow is pictured below:
```mermaid
---
config:
  htmlLabels: false
---
flowchart LR;
    subgraph "MIPS Program"
        S["`Source
        Code
        *.s*`"]
    end
    subgraph "LogiSpim Application"
        C["Compile
        Tool"]
        Text["`Compiled Instructions
        *text.hex*`"]
        Data["`Compiled Data
        *data.hex*`"]
    end
    subgraph "LogiSpim Processor"
        IM["`Instruction
        Memory
        *.text*`"]
        MM["`Main
        Memory
        *.data*`"]
    end
    
    S --> C
    
    C --> Text
    C --> Data
    
    Text --> IM
    Data --> MM
```
</details>


## Installation and Usage
Guides for installing and using LogiSpim are linked below:
* [Installation Guide]
* [Usage Guide]

## Additional Resources
The microarchitecture of LogiSpim's processor was based primarily on [this textbook and its companion assets](https://shop.elsevier.com/books/book-companion/9780124077263),
notably with the instruction set based on its [MIPS reference card](https://brand.elsevier.com/share/6aARqA6Vi7mcaoV8DWQs/assets/98319).