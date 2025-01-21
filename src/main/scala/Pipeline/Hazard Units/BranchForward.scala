package Pipeline

import chisel3._
import chisel3.util._

class BranchForward extends Module {
  val io = IO(new Bundle {
    val ID_EX_RD    = Input(UInt(5.W))
    val EX_MEM_RD   = Input(UInt(5.W))
    val MEM_WB_RD   = Input(UInt(5.W))
    val ID_EX_memRd = Input(UInt(1.W))
    val EX_MEM_memRd = Input(UInt(1.W))
    val MEM_WB_memRd = Input(UInt(1.W))
    val rs1         = Input(UInt(5.W))
    val rs2         = Input(UInt(5.W))
    val ctrl_branch = Input(UInt(1.W))

    val forward_rs1 = Output(UInt(4.W))
    val forward_rs2 = Output(UInt(4.W))
  })
  io.forward_rs1 := "b0000".U
  io.forward_rs2 := "b0000".U

  // Branch forwarding logic
  when(io.ctrl_branch === 1.U) {
    // ALU Hazard
    when(io.ID_EX_RD =/= 0.U && io.ID_EX_memRd =/= 1.U) {
      when(io.ID_EX_RD === io.rs1 && io.ID_EX_RD === io.rs2) {
        io.forward_rs1 := "b0001".U
        io.forward_rs2 := "b0001".U
      }.elsewhen(io.ID_EX_RD === io.rs1) {
        io.forward_rs1 := "b0001".U
      }.elsewhen(io.ID_EX_RD === io.rs2) {
        io.forward_rs2 := "b0001".U
      }
    }

    // EX/MEM Hazard
    when(io.EX_MEM_RD =/= 0.U && io.EX_MEM_memRd =/= 1.U) {
      when(io.EX_MEM_RD === io.rs1 && io.EX_MEM_RD === io.rs2 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1 && io.ID_EX_RD === io.rs2)) {
        io.forward_rs1 := "b0010".U
        io.forward_rs2 := "b0010".U
      }.elsewhen(io.EX_MEM_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1)) {
        io.forward_rs1 := "b0010".U
      }.elsewhen(io.EX_MEM_RD === io.rs2 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs2)) {
        io.forward_rs2 := "b0010".U
      }
    }

    // MEM/WB Hazard
    when(io.MEM_WB_RD =/= 0.U && io.MEM_WB_memRd =/= 1.U) {
      when(io.MEM_WB_RD === io.rs1 && io.MEM_WB_RD === io.rs2 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1 && io.ID_EX_RD === io.rs2) && !(io.EX_MEM_RD =/= 0.U && io.EX_MEM_RD === io.rs1 && io.EX_MEM_RD === io.rs2)) {
        io.forward_rs1 := "b0011".U
        io.forward_rs2 := "b0011".U
      }.elsewhen(io.MEM_WB_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1) && !(io.EX_MEM_RD =/= 0.U && io.EX_MEM_RD === io.rs1)) {
        io.forward_rs1 := "b0011".U
      }.elsewhen(io.MEM_WB_RD === io.rs2 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs2) && !(io.EX_MEM_RD =/= 0.U && io.EX_MEM_RD === io.rs2)) {
        io.forward_rs2 := "b0011".U
      }
    }

  // Jalr forwarding logic
  }.elsewhen(io.ctrl_branch === 0.U) {
    when(io.ID_EX_RD =/= 0.U && io.ID_EX_memRd =/= 1.U && io.ID_EX_RD === io.rs1) {  //ID/EX Hazard => rd ≠ x0 、 not load 、 rs1前 = rd後 
      io.forward_rs1 := "b0110".U //6 Alu.out
    }.elsewhen(io.EX_MEM_RD =/= 0.U && io.EX_MEM_memRd =/= 1.U && io.EX_MEM_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1)) { //EX/Mem Hazard => rd ≠ x0 、 not load 、 rs1前 = rd後 、not ID/EX Hazard 
      io.forward_rs1 := "b0111".U //7 EX_MEM_M.io.EXMEM_alu_out
    }.elsewhen(io.EX_MEM_RD =/= 0.U && io.EX_MEM_memRd === 1.U && io.EX_MEM_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1)) { //load EX_Mem Hazard => rd ≠ x0 、 is load 、 rs1前 = rd後 、 not not load ID/EX Hazard
      io.forward_rs1 := "b1001".U //9 DataMemory.io.dataOut
    }.elsewhen(io.MEM_WB_RD =/= 0.U && io.MEM_WB_memRd =/= 1.U && io.MEM_WB_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1) && !(io.EX_MEM_RD =/= 0.U && io.EX_MEM_RD === io.rs1)) { //Mem/WB Hazard => rd ≠ x0 、 not load 、 rs1前 = rd後 、not ID/EX Hazard、not ID/EX Hazard 
      io.forward_rs1 := "b1000".U //8 RegFile.io.w_data
    }.elsewhen(io.MEM_WB_RD =/= 0.U && io.MEM_WB_memRd === 1.U && io.MEM_WB_RD === io.rs1 && !(io.ID_EX_RD =/= 0.U && io.ID_EX_RD === io.rs1) && !(io.EX_MEM_RD =/= 0.U && io.EX_MEM_RD === io.rs1)) { //load MEM_WB Hazard => rd ≠ x0 、 is load 、 rs1前 = rd後 、 not not load ID/EX Hazard、not not load EX/Mem Hazard 
      io.forward_rs1 := "b1010".U //10 RegFile.io.w_data 
    }
  }
}