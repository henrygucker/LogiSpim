    .data
str: .asciiz "Hello, world!"

    .text
main:           # Entry point to program denoted by "main" label
    la $a0, str
    li $v0, 4
    syscall

    jr $ra      # Returns from the program, ending execution
