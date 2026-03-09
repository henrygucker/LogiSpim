    .data
    .text
_entry:
    jal main
    
    # exit program
    li $v0, 10
    syscall

main:
    jr $ra
