.text
.globl _start

_start:
    # 這裡是進入程式的最早位置
    # 先準備參數
    addi a0, x0, 3
    addi a1, x0, 5
    # 呼叫 sum_int
    jal sum_int

    # sum_int 結束後，回到這裡
    # (這裡要怎麼結束程式或死迴圈，看你需求)
    j .

sum_int:
    add t2, a0, a1
    addi a0, t2, 0
    jr ra
    
