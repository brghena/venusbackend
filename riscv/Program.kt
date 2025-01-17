package venusbackend.riscv

import venusbackend.assembler.AssemblerError
import venusbackend.assembler.DebugInfo
import venusbackend.linker.DataRelocationInfo
import venusbackend.linker.RelocationInfo
import venusbackend.riscv.insts.dsl.relocators.Relocator

/**
 * An (unlinked) program.
 *
 * @param name the name of the program, used for debug info
 * @see venus.assembler.Assembler
 * @see venus.linker.Linker
 */
public val venusInternalLabels: String = "Venus_Internal_Label-"
class Program(var name: String = "anonymous") {
    /* TODO: abstract away these variables */
    val insts = ArrayList<MachineCode>()
    val debugInfo = ArrayList<DebugInfo>()
    val labels = HashMap<String, Int>()
    val relocationTable = ArrayList<RelocationInfo>()
    val dataRelocationTable = ArrayList<DataRelocationInfo>()
    val dataSegment = ArrayList<Byte>()
    val localReferences = HashMap<Int, MutableSet<Int>>() // <Numeric Label, Addresses of that label>
    var textSize = 0
    var dataSize = 0
    private val globalLabels = HashSet<String>()
    val imports = ArrayList<String>()

    /**
     * Adds an instruction to the program, and increments the text size.
     *
     * @param mcode the instruction to add
     */
    fun add(mcode: MachineCode) {
        insts.add(mcode)
        textSize += mcode.length
    }

    /**
     * Adds a byte of data to the program, and increments the data size.
     *
     * @param byte the byte to add
     */
    fun addToData(byte: Byte) {
        dataSegment.add(byte)
        dataSize++
    }

    /**
     * Overwrites a byte of data in the program's data segment
     *
     * @param offset the offset at which to overwrite
     * @param byte the value to overwrite with
     */
    fun overwriteData(offset: Int, byte: Byte) {
        dataSegment[offset] = byte
    }

    fun addImport(filepath: String) {
        this.imports.add(filepath)
    }

    /**
     * Adds debug info to the instruction currently being assembled.
     *
     * In the case of pseudo-instructions, the original instruction will be added multiple times.
     * @todo Find a better way to deal with pseudoinstructions
     *
     * @param dbg the debug info to add
     */
    fun addDebugInfo(dbg: DebugInfo) {
        while (debugInfo.size < insts.size) {
            debugInfo.add(dbg)
        }
    }

    /**
     * Adds a label with a given offset to the program.
     *
     * @param label the label to add
     * @param offset the byte offset to add it at (from the start of the program)
     */
    fun addLabel(label: String, offset: Int): Int? {
        if (label.matches(Regex("\\d+"))) {
            val intlabel = label.toInt()
            if (localReferences.containsKey(intlabel)) {
                val set = localReferences[intlabel]!!
                set.add(offset)
            } else {
                val new_set = HashSet<Int>()
                new_set.add(offset)
                localReferences[intlabel] = new_set
            }
            return null
        } else {
            return labels.put(label, offset)
        }
    }

    /**
     * Gets the _relative_ label offset, or null if it does not exist.
     *
     * The _relative_ offset is relative to the instruction currently being assembled.
     *
     * @param label the label to find
     * @returns the relative offset, or null if it does not exist.
     */
    fun getLabelOffset(label: String, address: Int): Int? {
        // TODO FIX ME TO WORK WITH FORWARD AND BACKWARD LOCAL REFERENCE
        val loc = if (label.matches(Regex("\\d+[fb]"))) {
            val intlabel = label.substring(0, label.length - 1).toInt()
            val number_set = localReferences[intlabel] ?: throw AssemblerError("The number label '$intlabel' has not been defined!")
            if (label.matches(Regex("\\d+f"))) {
                number_set.filter { it >= address }.min()
            } else {
                number_set.filter { it <= address }.max()
            }
        } else {
            labels.get(label)
        }
        return loc?.minus(textSize)
    }

    /**
     * Adds a line to the relocation table.
     *
     * @param label the label to relocate
     * @param offset the byte offset the label is at (from the start of the program)
     */
    fun addRelocation(relocator: Relocator, label: String, offset: Int = textSize) =
            relocationTable.add(RelocationInfo(relocator, offset, label))

    /**
     * Adds a line to the data relocation table.
     *
     * @param label the label to relocate
     * @param offset the byte offset the label is at (from the start of the data section)
     */
    fun addDataRelocation(label: String, offset: Int = textSize) =
            dataRelocationTable.add(DataRelocationInfo(offset, label))

    /**
     * Makes a label global.
     *
     * @param label the label to make global
     */
    fun makeLabelGlobal(label: String) {
        globalLabels.add(label)
    }

    /**
     * Checks if a label is global.
     *
     * @param label the label to check
     * @return true if the label is global
     */
    fun isGlobalLabel(label: String) = globalLabels.contains(label)

    /* TODO: add dump formats */
    /**
     * Dumps the instructions.
     *
     * @return a list of instructions in this program
     */
    fun dump(): List<MachineCode> = insts

    fun assembleDependencies(): ArrayList<Program> {
        return ArrayList()
    }
}
