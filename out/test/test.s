	.data
	nums: .word 1, 2, 3, 4, 5, 6, 7, 8, 9
	str: .asciiz "thingy"
	.text
_entry:
	jal main

	# exit program
	li $v0, 10
	move $k0, $ra
	syscall
	nop

	main:
	addi $sp, $sp, -8
	addi $s1, $0, 255
	
	sw $ra, 4($sp)
	sw $s1, 0($sp)
	
	add $s0, $0, $0
	
	jr $ra
