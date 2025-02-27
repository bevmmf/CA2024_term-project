## Credits / Original Author

This project is based on or derived from the [5-stage-RV32I](https://github.com/kinzafatim/5-Stage-RV32I).  
Special thanks to **@kinzafatim** for providing the original code and license.
# 5-Stage Pipeline Processor in Chisel(from kinzafatim)

This repository contains a 5-stage pipeline processor implemented using the Chisel hardware description language. The processor follows the classic RISC-V 5-stage pipeline design, which includes the following stages:

- **Instruction Fetch (IF)**
- **Instruction Decode (ID)**
- **Execute (EX)**
- **Memory Access (MA)**
- **Write Back (WB)**

## Project Structure

- **Top.scala**: The top module that integrates all the stages of the pipeline.
- **IF.scala**: The Instruction Fetch (IF) stage that fetches instructions from memory.
- **ID.scala**: The Instruction Decode (ID) stage that decodes the fetched instructions and prepares operands for execution.
- **EX.scala**: The Execute (EX) stage where arithmetic and logical operations are performed.
- **MA.scala**: The Memory Access (MA) stage that handles data memory read and write operations.
- **WB.scala**: The Write Back (WB) stage that writes results back to the register file.

## Pipeline Registers

Pipeline registers are used between stages to store intermediate data and control signals. This ensures that each stage of the pipeline operates independently and in parallel with the others.

- **IF/ID Pipeline Registers**: These registers hold the instruction and program counter values between the IF and ID stages.
- **ID/EX Pipeline Registers**: These registers pass decoded instruction signals and operands from the ID stage to the EX stage.
- **EX/MEM Pipeline Registers**: These registers carry the results of the EX stage to the MA stage.
- **MEM/WB Pipeline Registers**: These registers transfer the data from the MA stage to the WB stage for final write-back to the register file.

## Top Module Overview

The Top module connects all the stages of the pipeline and handles the flow of data through the pipeline registers. Below is a brief overview of its components:

- **Input/Output**:
  - `in`: Input signal to the processor.
  - `out`: Output signal from the processor.

- **Pipeline Stages**:
  - `fetch`: Instruction Fetch stage.
  - `decoder`: Instruction Decode stage.
  - `execute`: Execute stage.
  - `memory`: Memory Access stage.
  - `writeBack`: Write Back stage.

- **Pipeline Registers**:
  - Registers between each stage are used to pass data and control signals.

- **Control Logic**:
  - Various Mux and control signals ensure correct data flow and pipeline operation.

more specific info is at https://hackmd.io/@sysprog/Hkh1t9hSkg

