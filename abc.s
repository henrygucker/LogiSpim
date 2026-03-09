    .data
str: .asciiz "Hello, world!\n"
arr: .word 5, 6, 5, 4
    .text
main:
    la $s0, str
    la $s1, arr

    nop
    move $k0, $ra
    nop

    sw $s0, 0($s1)
    lw $s2, 0($s1)
    lwl $s2, 3($s1)
    lw $s2, 8($s1)
    lw $s2, 12($s1)

    nop
    addi $s3, $s2, 10

    nop
    nop
    add $s4, $s3, $s2




