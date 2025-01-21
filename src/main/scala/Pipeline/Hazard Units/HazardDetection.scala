package Pipeline
import chisel3._
import chisel3.util._

//only check for load-use hazard
/*check pre_instr (ID_EX) if lw ,and (rd == curr_instr (IF_ID)  rs1 、 rs2)
  => stall 1 cycle, in case read the old data*/
class HazardDetection extends Module {
  val io = IO(new Bundle {
    //from IF/ID pipeline register
    val IF_ID_inst = Input(UInt(32.W))
    //from ID/EX pipeline register
    val ID_EX_memRead = Input(Bool())  //check if pre_instr is lw
    val ID_EX_rd = Input(UInt(5.W))    //
    //PC
    val pc_in = Input(SInt(32.W))
    val current_pc = Input(SInt(32.W))
    //if stall / bubble?
    val inst_forward = Output(Bool())
    val pc_forward = Output(Bool())
    val ctrl_forward = Output(Bool())
    // pass-through
    val inst_out = Output(UInt(32.W))
    val pc_out = Output(SInt(32.W))
    val current_pc_out = Output(SInt(32.W))
  })

  val Rs1 = io.IF_ID_inst(19, 15)
  val Rs2 = io.IF_ID_inst(24, 20)

  when(io.ID_EX_memRead === 1.B && ((io.ID_EX_rd === Rs1) || (io.ID_EX_rd === Rs2))) {
    io.inst_forward := true.B
    io.pc_forward := true.B
    io.ctrl_forward := true.B
  }.otherwise {
    io.inst_forward := false.B
    io.pc_forward := false.B
    io.ctrl_forward := false.B
  }
  io.inst_out := io.IF_ID_inst
  io.pc_out := io.pc_in
  io.current_pc_out := io.current_pc
}