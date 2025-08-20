package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers.extractTinyWordFromLineStart
import java.io.File
import java.nio.ByteBuffer

class NaspaWordList private constructor(
    val entries: LongMappedLines,
) : MemoryMappedDefinitionLookup(entries) {
    constructor(file: File) : this(parseWordLineRanges(file))

    override fun parseLine(line: ByteBuffer): WordDefinition {
        TODO("Not yet implemented")
    }

    companion object {
        private fun parseWordLineRanges(file: File) = LongMappedLines.create(
            file,
            { buffer, lineStart, lineEndInclusive ->
                buffer.extractTinyWordFromLineStart(' ', lineStart, lineEndInclusive).packed
            }
        )
    }
}