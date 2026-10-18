    .data # .data section for storing statically allocated values
str: .asciiz "Hello, world!\n"

    .text # .text section where instructions go

main: # The main label is the starting point of your program, coming directly before the first instruction to be executed.
    la $a0, str
    li $v0, 4
    syscall

    jr $ra # This exits your program and stops execution of the processor.