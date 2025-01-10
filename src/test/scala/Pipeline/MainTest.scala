package Pipeline
import chisel3._
import org.scalatest.FreeSpec
import chiseltest._

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

class DebugPortTest extends FreeSpec with ChiselScalatestTester {
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
