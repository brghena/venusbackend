package venusbackend.assembler.pseudos

/* ktlint-disable no-wildcard-imports */
import venusbackend.assembler.*
/* ktlint-enable no-wildcard-imports */
import venusbackend.riscv.insts.dsl.parsers.regNameToNumber
import venusbackend.riscv.insts.dsl.relocators.PCRelHiRelocator
import venusbackend.riscv.insts.dsl.relocators.PCRelLoRelocator

/**
 * Writes a load pseudoinstruction. (Those applied to a label)
 */
object Load : PseudoWriter() {
    override operator fun invoke(args: LineTokens, state: AssemblerPassOne): List<LineTokens> {
        checkArgsLength(args, 3)

        try {
            regNameToNumber(args[2])
            p1warnings.add(AssemblerWarning("You are using the load pseudoinstruction which takes in rd, symbol and the symbol matches a register name."))
        } catch (e: AssemblerError) {}

        val auipc = listOf("auipc", args[1], "0")
        state.addRelocation(PCRelHiRelocator, state.getOffset(), args[2])

        val load = listOf(args[0], args[1], "0", args[1])
        state.addRelocation(PCRelLoRelocator, state.getOffset() + 4, args[2])

        return listOf(auipc, load)
    }
}
