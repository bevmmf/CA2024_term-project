# Function: square
# Computes the square of an integer (a0 = n), returns result in a0
.text
.globl _start
_start:
		li sp, 0x100 
		addi a0,x0,3
		jal square
spin:
		beq x0,x0,spin

square:
    #addi sp, sp, -8         # Allocate stack space
    #sw ra, 0(sp)            # Save return address 

    add t0, x0, x0          # t0 = 0 (accumulator for the result)
    add t1, a0, x0          # t1 = a0 (copy of n, multiplicand)
    add t2, a0, x0          # t2 = a0 (copy of n, multiplier)

square_loop:
    andi t3, t2, 1          # Check the lowest bit of t2 (t2 & 1) 
    beq t3, x0, skip_add    # If the bit is 0, skip addition
    add t0, t0, t1          # Accumulate: t0 += t1

skip_add:
    slli t1, t1, 1           # Left shift t1 (multiply by 2) 
    srli t2, t2, 1           # Right shift t2 (divide by 2) 
    bne t2, x0, square_loop # Repeat loop if t2 is not zero

square_end:
    add a0, t0, x0          # Move result to a0

    #lw ra, 0(sp)            # Restore return address 
    #addi sp, sp, 8          # Deallocate stack space
    jalr x0, ra, 0          # Return from function 