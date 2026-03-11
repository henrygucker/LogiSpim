# Introduction
<!--suppress CssUnresolvedCustomProperty -->
<style> .mdbook-version { position: absolute; right: 20px; top: 60px; background-color: var(--theme-popup-bg); border-radius: 8px; padding: 2px 5px 2px 5px; border: 1px solid var(--theme-popup-border); font-size: 0.9em; } </style>
<p class="mdbook-version">Version: 0.1pre</p>

**LogiSpim** is a 32-bit MIPS processor built in [Logisim-Evolution](https://github.com/logisim-evolution/logisim-evolution)
with associated tooling for running single-file MIPS programs on the processor. This has been created as a tool to help
in learning pipelining in MIPS processors, and to offer a more in-depth experience in an introduction to micro-architecture.
The ISA implementation was inspired by [this MIPS data sheet](https://booksite.elsevier.com/9780124077263/downloads/COD_5e_Greencard.pdf).

### Features
* Assistive program for compiling and viewing MIPS instructions programs.
* Detailed documentation of micro-architecture for improved learning and understanding.
* Pipeline status view to easily display which instructions are in which parts of the pipeline at any given moment.
* Support for emulated console output through syscalls.
* Built-in breakpoint feature for debugging.

### Instructions
| Non R-Type Instructions                                                                                                                                        | R-Type Instructions |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| <table> <thead> <tr> <th>OP</th> <th>Name</th> <th>Status</th> </tr> </thead> <tbody><tr> <td>0</td> <td>NA</td> <td>No Instruction</td> </tr></tbody></table> | blank               |


#### Non R-Type Instructions
| OP | Name | Status         |
|:--:|:-----|:---------------|
| 0  | N/A  | No Instruction |
| 1  | N/A  | No Instruction |
| 2  | j    | Implemented    |
| 3  | j    | I              |
| 4  | j    | I              |
| 5  | j    | I              |
| 6  | j    | I              |
| 7  | j    | I              |
| 8  | j    | I              |
| 9  | j    | I              |
| 10 | j    | I              |
| 11 | j    | I              |
| 12 | j    | I              |
| 13 | j    | I              |
| 14 | j    | I              |
| 15 | j    | I              |
| 16 | j    | I              |
| 17 | j    | I              |
| 18 | j    | I              |
| 19 | j    | I              |
| 20 | j    | I              |
| 21 | j    | I              |
| 22 | j    | I              |
| 23 | j    | I              |
| 24 | j    | I              |
| 25 | j    | I              |
| 26 | j    | I              |
| 27 | j    | I              |
| 28 | j    | I              |
| 29 | j    | I              |
| 30 | j    | I              |
| 31 | j    | I              |
| 32 | j    | I              |
| 33 | j    | I              |
| 34 | j    | I              |
| 35 | j    | I              |
| 36 | j    | I              |
| 37 | j    | I              |
| 38 | j    | I              |
| 39 | j    | I              |
| 40 | j    | I              |
| 41 | j    | I              |
| 42 | j    | I              |
| 43 | j    | I              |
| 44 | j    | I              |
| 45 | j    | I              |
| 46 | j    | I              |
| 47 | j    | I              |
| 48 | j    | I              |
| 49 | j    | I              |
| 50 | j    | I              |
| 51 | j    | I              |
| 52 | j    | I              |
| 53 | j    | I              |
| 54 | j    | I              |
| 55 | j    | I              |
| 56 | j    | I              |
| 57 | j    | I              |
| 58 | j    | I              |
| 59 | j    | I              |
| 60 | j    | I              |
| 61 | j    | I              |
| 62 | j    | I              |
| 63 | j    | I              |

#### R-Type Instructions
| Funct | Name | Status         |
|:-----:|:-----|:---------------|
|   0   | N/A  | No Instruction |
|   1   | N/A  | No Instruction |
|   2   | j    | Implemented    |
|   3   | j    | Implemented    |
|   4   | j    | Implemented    |
|   5   | j    | Implemented    |
|   6   | j    | Implemented    |
|   7   | j    | Implemented    |
|   8   | j    | Implemented    |
|   9   | j    | Implemented    |
|  10   | j    | Implemented    |
|  11   | j    | Implemented    |
|  12   | j    | Implemented    |
|  13   | j    | Implemented    |
|  14   | j    | Implemented    |
|  15   | j    | Implemented    |
|  16   | j    | Implemented    |
|  17   | j    | Implemented    |
|  18   | j    | Implemented    |
|  19   | j    | Implemented    |
|  20   | j    | Implemented    |
|  21   | j    | Implemented    |
|  22   | j    | Implemented    |
|  23   | j    | Implemented    |
|  24   | j    | Implemented    |
|  25   | j    | Implemented    |
|  26   | j    | Implemented    |
|  27   | j    | Implemented    |
|  28   | j    | Implemented    |
|  29   | j    | Implemented    |
|  30   | j    | Implemented    |
|  31   | j    | Implemented    |
|  32   | j    | Implemented    |
|  33   | j    | Implemented    |
|  34   | j    | Implemented    |
|  35   | j    | Implemented    |
|  36   | j    | Implemented    |
|  37   | j    | Implemented    |
|  38   | j    | Implemented    |
|  39   | j    | Implemented    |
|  40   | j    | Implemented    |
|  41   | j    | Implemented    |
|  42   | j    | Implemented    |
|  43   | j    | Implemented    |
|  44   | j    | Implemented    |
|  45   | j    | Implemented    |
|  46   | j    | Implemented    |
|  47   | j    | Implemented    |
|  48   | j    | Implemented    |
|  49   | j    | Implemented    |
|  50   | j    | Implemented    |
|  51   | j    | Implemented    |
|  52   | j    | Implemented    |
|  53   | j    | Implemented    |
|  54   | j    | Implemented    |
|  55   | j    | Implemented    |
|  56   | j    | Implemented    |
|  57   | j    | Implemented    |
|  58   | j    | Implemented    |
|  59   | j    | Implemented    |
|  60   | j    | Implemented    |
|  61   | j    | Implemented    |
|  62   | j    | Implemented    |
|  63   | j    | Implemented    |
