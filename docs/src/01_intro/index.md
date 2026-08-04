<!--suppress CssUnresolvedCustomProperty -->
<style> .mdbook-version { position: absolute; right: 20px; top: 60px; background-color: var(--theme-popup-bg); border-radius: 8px; padding: 2px 5px 2px 5px; border: 1px solid var(--theme-popup-border); font-size: 0.9em; } </style>
<p class="mdbook-version">Version: 1.0.0-SNAPSHOT</p>

> [!WARNING]
> This website and project is currently under construction.
> Progress is being made, and will eventually be released once a satisfactory amount of work has been done.
> **Expected initial release during the Fall Semester of 2026**.

# Introduction

**LogiSpim** is a 32-bit MIPS processor built in [Logisim-Evolution](https://github.com/logisim-evolution/logisim-evolution)
support for running single-file MIPS programs on the processor from their source code. This has been created as a tool to help
in learning pipelining in MIPS processors, and to offer a more in-depth experience in an introduction to micro-architecture.
The Instruction Set Architecture (ISA) implementation was based on [this MIPS data sheet](https://booksite.elsevier.com/9780124077263/downloads/COD_5e_Greencard.pdf).

## Features
* Assistive tool for compiling and viewing MIPS program binaries for import to the Logisim circuit.
* Detailed documentation of micro-architecture for building deep understanding.
* Pipeline status view to easily see the instructions in each part of the pipeline.
* Support for emulated console output through syscalls.
* Built-in breakpoints for debugging.

## Short Demo
This video demonstrates how LogiSpim can be used to see the flow of instructions throughout pipeline stages.
<!-- TODO: Create and insert demo video -->
<iframe width="560" height="315" src="https://www.youtube.com/embed/oznr-1-poSU?si=TMw9bmlvfhv8-TEj" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
