# The LogiSpim Project

The project of LogiSpim is an educational tool that allows for interactive learning about pipelined microarchitecture.
Through a custom program, users can run MIPS programs on a 32-bit, 5-stage pipelined MIPS CPU in Logisim-evolution.

By using cross compilation toolchains, the file processor program converts MIPS source code into formatted binary files
that are easily imported into Logisim-evolution for use by the processor:
```mermaid
flowchart LR;
    S["MIPS<br>Program"]
    F["File<br>Processor"]
    C["Logisim<br>Processor"]
    
    S -->|Source<br>Code File<br><i>.s</i>| F
    F -->|Compiled<br>Binary Files<br><i>.hex</i>| C
```

For more information on the [logisim processor](01_logisim_processor.md) and the [file processor](02_file_processor.md),
refer to the following subchapters.