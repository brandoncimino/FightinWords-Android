package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.utf8
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import java.io.File

class DefinitionsCsvLookup private constructor(
    entries: LongMappedLines,
    private val language: KnownLanguage = KnownLanguage.English,
) : MemoryMappedDefinitionLookup(entries) {
    constructor(file: File) : this(parseWordLineRanges(file))

    override fun parseLine(rawEntry: ByteSlice): WordDefinition {
        return WordLookupHelpers.parseDefinitionCsvLine(rawEntry.toByteBuffer().utf8(), language)
    }

    companion object {
        private val commaByte: Byte = ','.code.toByte()
        private fun parseWordLineRanges(file: File) = LongMappedLines.create(
            file,
            { buffer, lineStart, lineEndInclusive ->
                TinyWord.extractTinyWordFromRange(
                    commaByte,
                    lineStart,
                    lineEndInclusive,
                    buffer::get
                ).packed
            }
        )
    }
}