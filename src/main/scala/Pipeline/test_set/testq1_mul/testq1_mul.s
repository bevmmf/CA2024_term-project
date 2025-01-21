.text
.globl _start
_start:
    addi a1,x0,-7             # multiplier
    addi a3,x0,9              # multiplicand
    li t0, 0                  # Initialize accumulator = addi t0,x0,0
    li t1, 32                 # Set bit counter (#A01) = addi t1,x0,32

    # Check for negative values
    bltz a1, handle_negative1 # If multiplier negative (#A02) = blt a1,x0,handle_negative1
    j shift_and_add_loop      # Skip to main loop (#A05)

handle_negative1:
    neg a1, a1                # Make multiplier positive = sub a1,x0,a1

handle_negative2:
    neg a3, a3                # Make multiplicand positive = sub a3,x0,a3

shift_and_add_loop:
    beqz t1, end_shift_and_add # Exit if bit count is zero = beq t1,x0,end_shift_and_add
    andi t2, a1, 1            # Check least significant bit (#A06)
    beqz t2, skip_add         # Skip add if bit is 0 = beq t2,x0,skip_add
    add t0, t0, a3            # Add to accumulator

skip_add:
    srai a1, a1, 1            # Right shift multiplier
    slli a3, a3, 1            # Left shift multiplicand
    addi t1, t1, -1           # Decrease bit counter
    j shift_and_add_loop      # Repeat loop (#A07)

end_shift_and_add:
    add a4, t0, x0              # Store final result(accumulator) in a4
loop:
    beq x0, x0, loop
