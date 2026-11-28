# Usage Setup

Prior to running a program with LogiSpim, ensure it meets the criteria of a valid MIPS program.

## MIPS Program
Generally, if a MIPS simple program works in [QtSpim](https://spimsimulator.sourceforge.net), it works in LogiSpim.

<details> <summary><h4 style="display: inline; margin: 0;">Program Requirements</h4></summary><br> 

1. It must have a `main` label in the `.text` segment.
   * This is handled the same way as in [QtSpim](https://spimsimulator.sourceforge.net).
   * The `main` label serves as the entry point for a LogiSpim program.
   * To end the program's execution, the main program must return to the address stored in `$ra` following the initial jump to `main`.
2. It must only use `.text` and `.data` segments.
   * LogiSpim does not yet support more specialized segments such as `.rodata` or `.bss`.
3. It must only use instructions implemented by LogiSpim (see [FAQ](../../05_faq/02_processor_faq/index.md#supported-instructions)).
   * While it does implement most instructions, LogiSpim does not yet support **all** MIPS instructions or syscall codes.
</details>

## Example Program
An example program is the following "Hello, world!" program, which is used throughout the remainder of this usage guide.
```asm
{{#include assets/hello_world.s }}
```
