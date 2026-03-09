# Starting address of the data segment is 0x10010000
# Starting address of text segement is 0x40000000
    .rdata
str: .asciiz "Result = "
        # What is the address of the last character in the string?
        # Answer: 0x10010009
arr: .word 1, 2, 3, 4, 5, 0
        # what is the address of arr[0]?
        # Answer: 0x1001000c

        # what is the address of arr[3]?
        # Answer: 0x10010018

    .text
main:
    addi $sp, $sp, -4
    sw $ra, 0($sp)

    jal compare
    move $t0, $v0

    # print "Result = "
    li $v0, 4
    la $a0, str
    syscall

    # print result
    li $v0, 1
    move $a0, $t0
    syscall

    lw $ra, 0($sp)

compare:
    jr $ra

