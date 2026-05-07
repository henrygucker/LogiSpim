# Video Guide
This video walks through the steps outlined below for running a program with LogiSpim.
<iframe width="560" height="315" src="https://www.youtube.com/embed/oznr-1-poSU?si=TMw9bmlvfhv8-TEj" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

### Example Program Used
```asm
    .data
str: .asciiz "Hello, world!"

    .text
main:           # Entry point to program denoted by "main" label
    la $a0, str
    li $v0, 4
    syscall     # Loads address to and prints str to the emulated console

    jr $ra      # Returns from the program, ending execution
```
> [!TIP]
> For more information on the criteria MIPS programs must follow to be run with LogiSpim,
> [see this section](3_2_written_guide.md#mips-program).
