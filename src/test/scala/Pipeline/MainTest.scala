package Pipeline
import chisel3._
import org.scalatest.FreeSpec
import chiseltest._

class TOPTest1 extends FreeSpec with ChiselScalatestTester{
   "5-Stage test with test.txt" in{
    test(new PIPELINE("src/main/scala/Pipeline/test.txt")){
        x =>
        x.clock.step(200) 
       }
   }
}


class TOPTest2 extends FreeSpec with ChiselScalatestTester{
   "5-Stage test with test2.txt" in{
    test(new PIPELINE("src/main/scala/Pipeline/test2.txt")){
        x =>
        x.clock.step(200) 
       }
   }
}
