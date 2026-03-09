    .data
arr: .space 20
    .text
main:

    la $s0, arr # $s0 = *arr[0]
    li $s1, 0

    jal init_arr_for

    nop
    nop

init_arr_for:
    slti $t1, $s1, 80

    nop
    nop

    beq $t1, $0, init_arr_for_done
    
    add $t0, $s0, $s1 # $t0 = &arr[i]

    nop

    sra $s2, $s1, 2

    nop
    nop

    sw $s2, 0($t0) # arr[i] = i;

    addi $s1, $s1, 4


    jr $ra

init_arr_for_done:
    addi $sp, $sp, -20

    

    

    
