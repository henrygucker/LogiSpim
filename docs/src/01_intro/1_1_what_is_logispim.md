# The LogiSpim Project

The project of LogiSpim is an educational tool that allows for interactive learning about pipelined microarchitecture.
The core usage of the project allows users to run MIPS programs on a 32-bit MIPS CPU in Logisim-Evolution.
This process is accomplished through the following process:

```mermaid
flowchart LR;
    S["MIPS<br>Program"]
    F["File<br>Processor"]
    C["Logisim<br>Processor"]
    
    S -->|Source<br>Code File<br><i>.s</i>| F
    F -->|Compiled<br>Binary Files<br><i>.hex</i>| C
```

## Logisim Processor
The core of the LogiSpim project is the 32-bit MIPS processor built in Logisim-Evolution.
This processor is able to run most MIPS programs and includes the ability to emulate syscalls for easy program
output directly in Logisim-Evolution.

### Pipeline Overview
This processor is broken up into 5 pipeline stages, each separated by designated registers.
Since each stage has its own purpose, this allows for multiple instructions to be executed at the same time.
In real processors, this is done to improve performance since it allows for a significantly higher frequency clock to be used while
still completing an instruction almost every clock cycle.

```mermaid
flowchart LR;
    F["Stage 1<br>Fetch"]
    D["Stage 2<br>Decode"]
    E["Stage 3<br>Execute"]
    M["Stage 4<br>Memory Access"]
    W["Stage 5<br>Writeback"]
    
    F --> D
    D --> E
    E --> M
    M --> W
```

This is the standard outline for a 5-stage pipelined MIPS processor, and is what LogiSpim is based on.
The process of executing an instruction with this pipelining is as follows:
1. Instruction is fetched from memory
2. The instruction is decoded and the relevant registers are read
3. Arithmetic operations are executed by the ALU
4. Main Memory is either read from or written to
5. Resulting value is written to its destination register

Each step of this outline is done with its respective pipeline stage.
However, this pipelining causes issues when the order of operations for certain tasks is disturbed.
The complexity this creates is why LogiSpim can help.
More information on the pipelining of this processor is provided [here](../05_pipeline_structure/index.md).

### Visual Demonstration
This demo covers how LogiSpim can be used to learn how pipelining concepts can be implemented to improve processor
performance. 
<!-- TODO: Insert Video -->
<iframe width="560" height="315" src="https://www.youtube.com/embed/oznr-1-poSU?si=TMw9bmlvfhv8-TEj" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>


## File Processor
The LogiSpim File Processor is what allows users to take their MIPS assembly source code and run it on the processor.
The File Processor uses cross-compilation toolchains and custom linker scripts to compile MIPS assembly code for the
Logisim-Evolution circuit and extract contents of separate memory segments which are easily imported into Logisim-Evolution.

### Visual Demonstration
This is a brief overview of the File Processor tool and how it can be used to easily make source code runnable on LogiSpim.
<!-- TODO: Insert Video -->
<iframe width="560" height="315" src="https://www.youtube.com/embed/oznr-1-poSU?si=TMw9bmlvfhv8-TEj" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

### &#9888;&#65039; Important Notice
Steps for installing and using the File Processor are given [here](../02_getting_started/index.md).
This ensures that all prerequisite tooling is present and that LogiSpim is installed correctly.
**Without taking these steps, the File Processor will not function properly.**