    .data # .data section for storing statically allocated values
str: .asciiz "Hello, world!\n"

    .text # .text section where instructions go
main: # starting point of your program

    # print str
    la $a0, str
    li $v0, 4
    syscall

    # returns from your main function and stops execution of the processor
    jr $ra