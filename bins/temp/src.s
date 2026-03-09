# Henry Gucker
    .data
arr: .byte 'E', 'L', 'V', 'I', 'S'
    .text
_entry:
jal main
li $v0, 10
move $k0, $ra
syscall
main:
    li $t0, 0 # i = 0
    li $t1, 5 # # const t1 = 5

    la $s0, arr

for:
    bge $t0, $t1, done
    
    add $s1, $s0, $t0 # $s1 = &arr[i] = $t0($s0)
    lb $s2, 0($s1) # $s2 = arr[i]


    # If either are true, perform if
    li $t2, 'G'
    ble $s2, $t2, if

    li $t2, 'M'
    bge $s2, $t2, if

    # Otherwise skip if
    j skip_if

if:
    # Makes char lowercase
    addi $s2, $s2, 32
    sb $s2, 0($s1)

skip_if:
    # Print current char
    li $v0, 11
    move $a0, $s2
    move $k0, $ra
    syscall
   nop

    addi $t0, $t0, 1

    j for

done:
    # Print newline
    li $v0, 11
    li $a0, '\n'
    move $k0, $ra
    syscall
   nop
    
    li $v0, 1
    li $a0, -12345678
    move $k0, $ra
    syscall
   nop


    li $v0, 0
    jr $ra
