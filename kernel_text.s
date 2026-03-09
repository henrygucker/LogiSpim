    .text
# function called with syscall instruction
syscall:
    addiu $sp, $sp, -20
    sw $ra, 16($sp)
    sw $t3, 12($sp)
    sw $t2, 8($sp)
    sw $t1, 4($sp)
    sw $t0, 0($sp)

    li $t0, 1
    bne $v0, $t0, dont_print_integer

    jal print_integer
    j return

dont_print_integer:
    li $t0, 4
    bne $v0, $t0, dont_print_string

    jal print_string
    j return

dont_print_string:
    li $t0, 10
    bne $v0, $t0, dont_exit_program

    jal exit
    j return

dont_exit_program:
    li $t0, 11
    bne $v0, $t0, dont_print_char

    jal print_char
    j return

dont_print_char:
    nop
return:
    lw $k1, 16($sp)
    lw $t3, 12($sp)
    lw $t2, 8($sp)
    lw $t1, 4($sp)
    lw $t0, 0($sp)
    addi $sp, $sp, 20

    move $ra, $k0
    jr $k1





# $v0 = 1
print_integer:
    # alloc stack space; bottom word for $ra, top 3 words for output string
    addiu $sp, $sp, 16
    sw $ra, 12($sp)

    li $t0, 10
    slt $t1, $a0, $0 # $t1 is a flag indicating the input is less than 0 (used for printing the negative sign or not)

    bne $a0, $0, print_integer_nonzero

    jal print_char

    j print_integer_end

print_integer_nonzero:
    bgtz $a0, print_integer_positive

    subu $a0, $0, $a0 # negates $a0 to make positive; $t1 already flagged it as negative

print_integer_positive:
    addi $t2, $sp, 11 # $t2 points to the current character in the string; string is built from end to start
    sb $0, 1($t2) # null terminating character

print_integer_get_next_char:
    beq $a0, $0, print_integer_end_string_building

    div $a0, $t0 # divide by 10
    mfhi $t3 # get remainder to $t3
    addi $t3, $t3, '0' # get ascii value of digit

    sb $t3, 0($t2)

    addi $t2, $t2, -1
    mflo $a0 # update $a0 with its divided value

    j print_integer_get_next_char


print_integer_end_string_building:
    beq $t1, $0, print_integer_print_string

    # prepends '-' to string
    li $t3, '-'
    sb $t3, 0($t2)

    addi $t2, $t2, -1

print_integer_print_string:
    # $t2 is pointing to the byte before front of the start of the string at this point
    addi $a0, $t2, 1

    jal print_string

print_integer_end:

    # load back $ra from the stack
    lw $ra, 12($sp)

    # zero out stack space used to not confuse users
    sw $0, 0($sp)
    sw $0, 4($sp)
    sw $0, 8($sp)
    sw $0, 12($sp)

    addi $sp, $sp, 16
    jr $ra

    

    



    



# $v0 = 4
print_string:
    move $t0, $a0 # $t0 holds pointer to current character

    # allocate stack space + place $ra on stack
    addiu $sp, $sp, -4
    sw $ra, 0($sp)

print_string_for:
    lb $a0, 0($t0) # $a0 holds ascii value
    beq $a0, $0, print_string_end_for

    jal print_char 

    addiu $t0, $t0, 1 # shift pointer one char

    beq $0, $0, print_string_for

print_string_end_for:
    lw $ra, 0($sp)
    sw $0, 0($sp) # Zero out used memory to not confuse users
    addiu $sp, $sp, 4

    jr $ra

    
    

# $v0 = 10
exit:
    nop # will modify binary to put custom instruction to suspend execution instead of nop (instr: 0x50000000, but 0x00000050 in memory due to little endian)
    beq $0, $0, return

# $v0 = 11
print_char:
    nop # will modify binary to put custom instruction to print the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)
    jr $ra

        

    

    
