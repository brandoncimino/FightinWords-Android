package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers.extractTinyWordFromLineStart
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class DefinitionsCsvLookup private constructor(
    val entries: LongMappedLines,
    private val language: KnownLanguage = KnownLanguage.English,
) : MemoryMappedDefinitionLookup(entries) {
    constructor(file: File) : this(parseWordLineRanges(file))

    override fun parseLine(line: ByteBuffer): WordDefinition {
        // TODO: Replace this with something that fails if the bytes aren't well-formed
        val lineString = StandardCharsets.UTF_8.decode(line)
        return WordLookupHelpers.parseDefinitionCsvLine(lineString, language)
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