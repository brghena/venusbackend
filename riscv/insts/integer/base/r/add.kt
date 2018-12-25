package venusbackend.riscv.insts.integer.base.r

import venusbackend.riscv.insts.dsl.RTypeInstruction

val add = RTypeInstruction(
        name = "add",
        opcode = 0b0110011,
        funct3 = 0b000,
        funct7 = 0b0000000,
        eval16 = { a, b -> (a + b).toShort() },
        eval32 = { a, b -> a + b },
        eval64 = { a, b -> a + b },
        eval128 = { a, b -> a + b }
)