    .data
num: .word -1
    .text
_entry:
    jal main
    
    # exit program
    li $v0, 10
    nop
    nop
    syscall

main:
    li $s0, 0xfedcba98
    li $s1, 0x01234567

    jr $ra

