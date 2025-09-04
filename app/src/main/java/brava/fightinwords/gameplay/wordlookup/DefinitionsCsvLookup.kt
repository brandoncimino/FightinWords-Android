package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.utf8
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers.extractTinyWordFromLineStart
import java.io.File
import java.nio.ByteBuffer

class DefinitionsCsvLookup private constructor(
    entries: LongMappedLines,
    private val language: KnownLanguage = KnownLanguage.English,
) : MemoryMappedDefinitionLookup(entries) {
    constructor(file: File) : this(parseWordLineRanges(file))

    override fun parseLine(line: ByteBuffer): WordDefinition {
        return WordLookupHelpers.parseDefinitionCsvLine(line.utf8(), language)
    }

    companion object {
        private fun parseWordLineRanges(file: File) = LongMappedLines.create(
            file,
            { buffer, lineStart, lineEndInclusive ->
                buffer.extractTinyWordFromLineStart(',', lineStart, lineEndInclusive).packed
            }
        )
    }
}