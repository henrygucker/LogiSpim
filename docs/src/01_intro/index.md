<!--suppress CssUnresolvedCustomProperty -->
<style> .mdbook-version { position: absolute; right: 20px; top: 60px; background-color: var(--theme-popup-bg); border-radius: 8px; padding: 2px 5px 2px 5px; border: 1px solid var(--theme-popup-border); font-size: 0.9em; } </style>
<p class="mdbook-version">v0.1.0-beta</p>


<div align="center">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="assets/LogiSpim_Logo_Light.svg">
      <source media="(prefers-color-scheme: light)" srcset="assets/LogiSpim_Logo_Dark.svg">
      <img alt="LogiSpim Logo" width="500" src="assets/LogiSpim_Logo_Both_Invert.svg">
    </picture>

[Overview][Get Started] | [Installation Guide] | [Usage Guide] | [FAQ][FAQ Page]
</div>

[Get Started]: https://henrygucker.github.io/LogiSpim/02_what_is_logispim/index.html
[Installation Guide]: https://henrygucker.github.io/LogiSpim/03_installation/index.html
[Usage Guide]: https://henrygucker.github.io/LogiSpim/04_usage_guide/index.html
[FAQ Page]: https://henrygucker.github.io/LogiSpim/05_faq/index.html

___

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
<details>
    <summary><h4 style="display: inline; margin: 0">Printing</h4></summary>

Printing to the emulated console through syscalls:

<img alt="Printing Demo" width="700" src="assets/printing_demo.gif">
</details>

<details>
    <summary><h4 style="display: inline; margin: 0">Clock Stepping & Assembly Viewer</h4></summary>

Manually stepping through instructions while zoomed into the processor's execute stage with the Assembly Viewer open:

<img alt="Stepping through instructions demo" width="700" src="assets/execute_stage_demo.gif">
</details>

## LogiSpim Application
The LogiSpim application enables users to run their own programs on LogiSpim's processor.
More information about the application, its role in the LogiSpim project, and installation guides can be
found [here](../02_what_is_logispim/index.md#logispims-novelty).

## Additional Resources
The microarchitecture of LogiSpim's processor was based primarily on [this textbook and its companion assets](https://shop.elsevier.com/books/book-companion/9780124077263),
notably with the instruction set based on its [MIPS reference card](https://brand.elsevier.com/share/6aARqA6Vi7mcaoV8DWQs/assets/98319).
