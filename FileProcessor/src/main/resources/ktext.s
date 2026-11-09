# This file contains the LogiSpim kernel
# Where specified, nops are overridden by a postprocessor to compile special instructions

    .text
# function called with syscall instruction
syscall:
    ori $k1, $0, 10
    bne $v0, $k1, dont_exit_program # early check to see if is exit program call to make exiting program take fewer cycles

# $v0 = 10 (exit program)
exit:
    nop # postprocessor modifies binary to put custom instruction to exit the program instead of nop (instr: 0x50000000, but 0x00000050 in memory due to little endian)

dont_exit_program:

    addiu $sp, $sp, -16
    sw $ra, 12($sp)
    sw $t2, 8($sp)
    sw $t1, 4($sp)
    sw $t0, 0($sp)

    ori $t0, $0, 1
    bne $v0, $t0, dont_print_integer

    jal print_integer
    j return

dont_print_integer:
    ori $t0, $0, 4
    bne $v0, $t0, dont_print_string

    jal print_string
    j return

dont_print_string:
    ori $t0, $0, 11
    bne $v0, $t0, dont_print_char

    jal print_char
    j return

dont_print_char:
return:
    # stack cleanup
    lw $k1, 12($sp) # $k1 is address of instruction following syscall (originally stored in $ra after syscall)
    lw $t2, 8($sp)
    lw $t1, 4($sp)
    lw $t0, 0($sp)

    sw $0, 12($sp)
    sw $0, 8($sp)
    sw $0, 4($sp)
    sw $0, 0($sp)

    addi $sp, $sp, 16

    move $ra, $k0 # $k0 holds value of $ra from before the syscall was executed
    nop
    jr $k1





# $v0 = 1
print_integer:
    # Preserves HI and LO registers in ALU
    addiu $sp, $sp, -8
    mfhi $t0
    mflo $t1

    sw $t0, 4($sp)
    sw $t1, 0($sp)


    move $t0, $a0 # operations use $t0 as current value, allowing $a0 to be used for printing characters
    move $t2, $a0 # clones $a0 for restoration later

    bne $t0, $0, print_integer_nonzero

    ori $a0, $0, '0'

print_integer_zero_inlined_print_char:
    nop # postprocessor puts custom instruction for printing the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)

    j print_integer_end

print_integer_nonzero:
    bgtz $t0, print_integer_positive

    subu $t0, $0, $t0 # negates argument int to make positive

    ori $a0, $0, '-'

print_integer_negative_inlined_print_char:
    nop # postprocessor puts custom instruction for printing the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)

print_integer_positive:
    move $a0, $t0
    move $k1, $t0 # stores positive argument in $k1

    ori $t0, $0, 10 # constant for dividing divisor by 10
    li $t1, 1000000000 # $t1 acts as divisor | max int of 2.1b, so first digit can be result when divided by 1b


    divu $a0, $t1

    bgtz $a0, print_integer_main_loop_prep

print_integer_scale_divisor:
    # restore $a0 from remainder of division
    mfhi $a0

    # divide divisor
    divu $t1, $t0

    # divide $a0
    divu $a0, $t1

    # branch if $a0 is NOT positive first digit
    blez $a0, print_integer_scale_divisor

print_integer_main_loop_prep:
    # restore $a0 with arg since it will be divided in loop
    move $a0, $k1

print_integer_main_loop:

    divu $a0, $t1 # divide by divisor and update $a0 with new value

    addiu $a0, $a0, '0' # get ascii value of digit


print_integer_curr_digit_inlined_print_char:
    nop # postprocessor puts custom instruction for printing the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)

    mfhi $a0 # load remainder to $a0

    divu $t1, $t0

    # continue if divisor ($t1) is nonzero
    bgtz $t1, print_integer_main_loop

print_integer_end:

    # restores HI and LO registers in ALU
    lw $t0, 4($sp)
    lw $t1, 0($sp)
    addiu $sp, $sp, 8

    mthi $t0
    mtlo $t1

    # restores $a0
    move $a0, $t2

    jr $ra











# $v0 = 4
print_string:
    move $t1, $a0 # copies $a0 to restore later
    move $t0, $a0 # $t0 holds pointer to current character

    lb $a0, 0($t0) # $a0 holds ascii value
    beq $a0, $0, print_string_end
    nop # ensures nop is inserted before print char instruction (blocking hard to debug edge-case possible based on compiler settings which vary by OS)

print_string_for:
print_string_inlined_print_char:
    nop # postprocessor puts custom instruction for printing the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)

    addiu $t0, $t0, 1 # shift pointer one char
    lb $a0, 0($t0) # $a0 holds ascii value

    bgtz $a0, print_string_for

print_string_end:
    move $a0, $t1

    jr $ra

    
    
# $v0 = 11
print_char:
    nop # postprocessor puts custom instruction for printing the character with ascii value of the least significant byte in $a0 instead of nop (instr: 0x54000000, but 0x00000054 in memory due to little endian)
    jr $ra

        

    

    
