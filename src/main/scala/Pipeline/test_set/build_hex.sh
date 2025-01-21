set -e  # 若有任何指令失敗，就中止腳本

if [ $# -lt 1 ]; then
  echo "Usage: $0 <input_asm_file.s>"
  exit 1
fi

INPUT_S=$1
BASENAME="${INPUT_S%.*}"   # 取掉副檔名，假設 input 是 test.s，則 BASENAME=test

echo "[1] Assembling..."
riscv32-unknown-elf-as "${INPUT_S}" -o "${BASENAME}.o"

echo "[2] Linking..."
riscv32-unknown-elf-ld "${BASENAME}.o" -o "${BASENAME}.elf"

echo "[3] Objcopy to pure binary..."
riscv32-unknown-elf-objcopy -O binary "${BASENAME}.elf" "${BASENAME}.bin"

echo "[4] Hexdump to one-instruction-per-line (test.mem)..."
hexdump -v -e '1/4 "%08X\n"' "${BASENAME}.bin" > "${BASENAME}.mem"

echo "Done! Output file: ${BASENAME}.mem"
