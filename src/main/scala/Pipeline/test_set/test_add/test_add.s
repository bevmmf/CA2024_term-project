.text
.globl  _start

_start:
        addi x3, x0, 10
        addi x4, x0, 32
        add  x5, x3, x4
loop:
        beq x0, x0, loop




