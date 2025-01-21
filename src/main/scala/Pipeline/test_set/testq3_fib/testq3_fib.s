.text
.globl _start
_start:
#   fib(0) = 0
#   fib(1) = 1
#   fib(n) = fib(n-1) + fib(n-2),  for n >= 2
#    input : n
#    output : fib(n)    
main:
    li a0,6  
    jal ra,fib
spin:
    beq x0,x0,spin
    
fib:
     addi sp, sp, -12
     sw ra, 0(sp)
     sw a0, 4(sp)
     sw s0, 8(sp)
     li s0, 0
     li a7, 2
if:  
     blt a0,a7,done
     
L2: 
     #fib(n-1)
     addi a0, a0, -1
     jal fib
     add s0, s0, a0  # s0 = fib(n-1)
     
     lw a0, 4(sp)
     #fib(n-2)
     addi a0, a0, -2
     jal fib
     mv t0, a0
     # t0 = fib(n-2)，s0 = fib(n-1)
     add a0, s0, t0
done:
     lw ra, 0(sp)
     lw s0, 8(sp)

L1:
     addi sp, sp, 12
     jalr x0, ra, 0