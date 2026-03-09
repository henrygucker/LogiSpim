    .data
one: .word 1
arr: .word 255, 2048, -1234, 4, 0
    .text
main: 
    lw $s0, 0x10000000

    lw $s1, 0x10000004
    lw $s2, 0x10000008
    beq $s1, $s2, skip_if

    lw $s3, 0x1000000c

skip_if:

   lw $s4, 0x10000010 


