# 5-Stage RV32I Pipeline CPU in Chisel

This project is a 5-stage pipelined RV32I CPU implemented in Chisel for a Computer Architecture course project. The repository is based on the open-source project [kinzafatim/5-Stage-RV32I](https://github.com/kinzafatim/5-Stage-RV32I). It was not created through GitHub's fork flow; the original project was downloaded, organized, pushed into this coursework repository, and then modified, tested, and extended here.

## Project Highlights

- Implements the classic 5-stage pipeline: IF, ID, EX, MEM, WB.
- Uses separate pipeline registers: IF/ID, ID/EX, EX/MEM, and MEM/WB.
- Supports a practical RV32I subset: R-type, I-type, load/store, branch, LUI, AUIPC, JAL, and JALR.
- Implements load-use hazard detection with stall and bubble insertion.
- Implements multiple forwarding paths, including ALU operand forwarding, branch forwarding, JALR forwarding, and MEM/WB-to-ID forwarding.
- Uses ChiselTest to run assembly programs and verify register results through a debug port.

## Architecture Overview

The top-level CPU is implemented in `src/main/scala/Pipeline/Main.scala` as `PIPELINE(initFile: String)`. The CPU loads instructions from the given instruction memory file and exposes `debug_read_reg` / `debug_reg_value` so the testbench can inspect register file state.

| Stage | Responsibility | Related files |
| --- | --- | --- |
| IF | PC update, PC+4, instruction fetch | `PC.scala`, `PC4.scala`, `InstMem.scala`, `IF_ID.scala` |
| ID | Instruction decode, register read, immediate generation | `Control.scala`, `RegisterFile.scala`, `ImmGenerator.scala` |
| EX | ALU control, ALU execution, ALU forwarding | `Alu_Control.scala`, `Alu.scala`, `Forwarding.scala` |
| MEM | Data memory read/write | `DataMemory.scala`, `EX_MEM.scala` |
| WB | Select ALU or memory result and write back to the register file | `MEM_WB.scala`, `RegisterFile.scala` |

## Supported Instruction Scope

The current RV32I instruction coverage includes:

- R-type: `add`, `sub`, `sll`, `slt`, `sltu`, `xor`, `srl`, `sra`, `or`, `and`
- I-type arithmetic/shift: `addi`, `slli`, `slti`, `sltiu`, `xori`, `srli`, `srai`, `ori`, `andi`
- Load/store: `lw`, `sw`
- Branch: `beq`, `bne`, `blt`, `bge`, `bltu`, `bgeu`
- Upper immediate / PC-relative: `lui`, `auipc`
- Jump: `jal`, `jalr`

This CPU is an educational RV32I pipeline CPU, not a production-grade RISC-V core. It does not currently implement CSR instructions, privilege modes, exceptions/interrupts, caches, virtual memory, or ISA extensions such as M/A/F/D.

## Hazard Handling

Hazard handling in this project is split into two major parts: **hazard detection** and **forwarding**. The main stall-based detection is for load-use hazards; most other RAW hazards are resolved through forwarding.

### 1. Hazard Detection

| Hazard / Control Case | Implementation file | Description |
| --- | --- | --- |
| Load-use data hazard detection | `src/main/scala/Pipeline/Hazard Units/HazardDetection.scala` | Detects whether the previous instruction in the ID/EX stage is a load and whether its `rd` matches the current IF/ID instruction's `rs1` or `rs2`. |
| Stall PC / instruction | `src/main/scala/Pipeline/Main.scala` | Uses `pc_forward` and `inst_forward` to keep the current PC and instruction stable for one cycle. |
| Insert bubble into ID/EX | `src/main/scala/Pipeline/Main.scala` | Uses `ctrl_forward` to clear the ID/EX control signals to zero, inserting a no-op bubble. |
| Control hazard flush | `src/main/scala/Pipeline/Main.scala` | Clears IF/ID inputs when a taken branch, JAL, or JALR redirects the PC, preventing wrong-path instructions from continuing. |

Core condition in `HazardDetection.scala`:

```scala
when(io.ID_EX_memRead === 1.B && ((io.ID_EX_rd === Rs1) || (io.ID_EX_rd === Rs2))) {
  io.inst_forward := true.B
  io.pc_forward := true.B
  io.ctrl_forward := true.B
}
```

In other words:

- The previous instruction in ID/EX is a load.
- The next instruction in IF/ID needs to read the same destination register.
- Since load data is not immediately available to the next instruction, the pipeline stalls for one cycle and inserts a bubble.

### 2. Forwarding

| Forwarding Path | Implementation file | Description |
| --- | --- | --- |
| EX/MEM -> EX ALU operand | `src/main/scala/Pipeline/Hazard Units/Forwarding.scala` | If EX/MEM `rd` matches ID/EX `rs1` or `rs2` and EX/MEM will write back, the EX/MEM ALU result is forwarded to the ALU input. |
| MEM/WB -> EX ALU operand | `src/main/scala/Pipeline/Hazard Units/Forwarding.scala` | If MEM/WB `rd` matches ID/EX `rs1` or `rs2` and MEM/WB will write back, writeback data is forwarded to the ALU input. |
| ALU input mux selection | `src/main/scala/Pipeline/Main.scala` | Uses `forward_a` and `forward_b` to select the actual ALU input A/B sources. |
| Branch operand forwarding | `src/main/scala/Pipeline/Hazard Units/BranchForward.scala` | Allows branch comparison operands to come from pending ID/EX, EX/MEM, or MEM/WB results before they are written back. |
| JALR base register forwarding | `src/main/scala/Pipeline/Hazard Units/BranchForward.scala` | Forwards the correct `rs1` value for JALR target calculation when the base register has not yet been written back. |
| Branch/JALR mux selection | `src/main/scala/Pipeline/Main.scala` | Uses `forward_rs1` and `forward_rs2` to select branch comparison operands or the JALR target base value. |
| MEM/WB -> ID forwarding | `src/main/scala/Pipeline/Hazard Units/StructuralHazard.scala` | If MEM/WB is writing an `rd` that matches decode-stage `rs1` or `rs2`, writeback data is used directly to avoid stale register values. |
| Decode-stage forwarding mux | `src/main/scala/Pipeline/Main.scala` | Uses `fwd_rs1` and `fwd_rs2` to choose whether ID/EX register data inputs come from the register file or writeback data. |

`Forwarding.scala` selector definition:

| Selector | Source |
| --- | --- |
| `00` | Original ID/EX register data |
| `01` | MEM/WB writeback data |
| `10` | EX/MEM ALU result |

EX/MEM forwarding has higher priority than MEM/WB forwarding, which prevents the ALU from using an older value when both stages match the same source register.

Additional logic in `BranchForward.scala`:

- Branch `rs1` / `rs2` forwarding.
- JALR `rs1` forwarding.
- Separate handling for ALU-result forwarding and load-result forwarding.

Although `StructuralHazard.scala` is named as a structural hazard unit, its actual role in this project is closer to **decode-stage MEM/WB forwarding**. It prevents stale register-file reads when writeback and decode happen around the same cycle.

## CPU Capability Compared With a Full RISC-V CPU

### Basic CPU Features Implemented

Compared with a complete RISC-V CPU, this project implements the following fundamental CPU features:

- 32-bit PC with sequential `PC + 4` update.
- Instruction memory fetch.
- Opcode decode and control signal generation.
- Register file read/write.
- Immediate generation for I, S, SB, U, and UJ formats.
- ALU arithmetic, logic, shift, and comparison operations.
- Load/store data memory path.
- Writeback path for ALU results or memory load results.
- Branch, JAL, and JALR control-flow updates.
- Complete 5-stage pipeline register structure.

### Extra Features Beyond a Minimal Pipeline

Beyond a minimal pipelined datapath, this project additionally implements:

- Load-use hazard detection.
- Stall and bubble insertion.
- EX/MEM and MEM/WB to EX-stage ALU forwarding.
- Branch comparison forwarding.
- JALR target forwarding.
- MEM/WB-to-ID forwarding.
- Program-level verification through a debug port.
- Multiple assembly/memory-image tests, including multiplication, square, Fibonacci, log2, bit reverse, and load/store stack debugging.

### Future Extensions

Possible extensions toward a more complete RISC-V CPU include:

- More complete RV32I compliance tests.
- CSR instructions.
- Exception, interrupt, and trap handling.
- Privilege modes: machine, supervisor, and user mode.
- Byte/halfword load/store instructions: `lb`, `lh`, `lbu`, `lhu`, `sb`, `sh`.
- RV32M extension: multiply, divide, and remainder.
- Cache, memory-mapped I/O, and bus interface support.
- Branch prediction and more complete pipeline flush/rollback logic.
- Performance counters and instruction trace/debug interfaces.
- FPGA synthesis, timing closure, and SoC integration.

## Verification

The main tests are in `src/test/scala/Pipeline/MainTest.scala`. Each test loads an instruction memory image, runs the CPU for a fixed number of cycles, and checks the resulting register value through the debug port.

| Test Program | Checked Register | Expected Result |
| --- | --- | --- |
| `testq1_mul.mem` | `x14(a4)` | `-63` |
| `test_set/debug/debug.mem` | `x10(a0)` | `1` |
| `test_set/debug_ins/debug_sw_lw_stack.txt` | `x1(ra)` | `7` |
| `test_set/testq2_square/testq2_square.mem` | `x10(a0)` | `9` |
| `test_set/testq3_fib/testq3_fib.mem` | `x10(a0)` | `8` |
| `test_set/testq4_log2/log2.mem` | `x10(a0)` | `3` |
| `test_set/testq5_bitreverse/bitreverse.mem` | `x10(a0)` | `3` |

Some tests use `WriteVcdAnnotation` to generate VCD waveforms, which helps inspect pipeline timing and hazard handling behavior.

## Repository Layout

```text
src/main/scala/Pipeline/
  Main.scala                  # Top-level pipelined CPU
  UNits/                      # ALU, control, branch, immediate, PC, register file
  Pipelines/                  # IF_ID, ID_EX, EX_MEM, MEM_WB pipeline registers
  Memory/                     # Instruction and data memory
  Hazard Units/               # Hazard detection and forwarding units
  test_set/                   # Assembly, ELF/bin, and memory images

src/test/scala/Pipeline/
  MainTest.scala              # ChiselTest regression tests
```

`src/main/scala/gcd` and `src/test/scala/gcd` are Chisel learning template/example files. The main CPU project is under `src/main/scala/Pipeline`.

## Build and Run

This project uses Scala 2.12.13, Chisel 3.5.4, and ChiselTest 0.5.4.

Compile:

```bash
sbt compile
```

Run pipeline tests:

```bash
sbt "testOnly Pipeline.DebugPortTest_testq"
```

Run all tests:

```bash
sbt test
```

## Attribution

This coursework repository is based on [kinzafatim/5-Stage-RV32I](https://github.com/kinzafatim/5-Stage-RV32I). The original code was downloaded and then pushed into this repository for course development. Subsequent work in this repository focuses on adapting, documenting, testing, and extending the pipeline implementation for the course project.
