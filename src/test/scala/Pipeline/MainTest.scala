package Pipeline
import chisel3._
import org.scalatest.freespec.AnyFreeSpec
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation

/*
class TOPTest extends FreeSpec with ChiselScalatestTester{
   "5-Stage test with test.txt" in{
    test(new PIPELINE("src/main/scala/Pipeline/test.txt")){
        x =>
        x.clock.step(200) 
       }
   }
   
   "5-Stage test with test2.txt" in{
    test(new PIPELINE("src/main/scala/Pipeline/test2.txt")){
        x =>
        x.clock.step(200) 
       }
   }
}
*/

/*
class DebugPortTest_instr extends FreeSpec with ChiselScalatestTester {
    "Test add instruction" in {
      test(new PIPELINE("src/main/scala/Pipeline/test_add.txt")) { dut =>
        dut.clock.step(50)
        dut.io.debug_read_reg.poke(5.U)
  
        val result = dut.io.debug_reg_value.peek()
        println(s"[TEST INFO] x5 = $result (decimal: ${result.litValue})")
  
        dut.io.debug_reg_value.expect(42.S)
      }
    }
  }
*/

class DebugPortTest_testq extends AnyFreeSpec with ChiselScalatestTester {
    "Testq1_shift and add mul" in {
      test(new PIPELINE("src/main/scala/Pipeline/testq1_mul.mem")) { dut =>
        dut.clock.step(700)
        dut.io.debug_read_reg.poke(14.U)
  
        val result = dut.io.debug_reg_value.peek()
        println(s"[TEST INFO] x14(a4) = $result (decimal: ${result.litValue})")
  
        dut.io.debug_reg_value.expect(-63.S)
      }
    }
  
    "debug" - {
      "check x10(a0)" in{
        test(new PIPELINE("src/main/scala/Pipeline/test_set/debug/debug.mem")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(500)
          dut.io.debug_read_reg.poke(10.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x10(a0) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(1.S)
        }
      }
    }
    "debug_sw_lw_stack" - {
      "check x11(a1)" in{
        test(new PIPELINE("src/main/scala/Pipeline/test_set/debug_ins/debug_sw_lw_stack.txt")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(25)
          dut.io.debug_read_reg.poke(1.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x1(ra) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(7.S)
        }
      }
    }
    "Testq2_square" - {
      "check x10(a0)" in {
        test(new PIPELINE("src/main/scala/Pipeline/test_set/testq2_square/testq2_square.mem")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(1000)
          dut.io.debug_read_reg.poke(10.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x10(a0) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(9.S)
        }
      }
    }
    "Testq3_fib" - {
      "check x10(a0)" in {
        test(new PIPELINE("src/main/scala/Pipeline/test_set/testq3_fib/testq3_fib.mem")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(1000)
          dut.io.debug_read_reg.poke(10.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x10(a0) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(8.S)
        }
      }
    }
    "Testq4_log2" - {
      "check x10(a0)" in {
        test(new PIPELINE("src/main/scala/Pipeline/test_set/testq4_log2/log2.mem")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(1000)
          dut.io.debug_read_reg.poke(10.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x10(a0) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(3.S)
        }
      }
    }
    "Testq5_bitreverse" - {
      "check x10(a0)" in {
        test(new PIPELINE("src/main/scala/Pipeline/test_set/testq5_bitreverse/bitreverse.mem")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          dut.clock.step(1000)
          dut.io.debug_read_reg.poke(10.U)
    
          val result = dut.io.debug_reg_value.peek()
          println(s"[TEST INFO] x10(a0) = $result (decimal: ${result.litValue})")
    
          dut.io.debug_reg_value.expect(3.S)
        }
      }
    }
}
