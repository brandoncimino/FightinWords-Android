package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import java.io.File
import java.nio.ByteBuffer

class NaspaWordList private constructor(
    entries: LongMappedLines,
) : MemoryMappedDefinitionLookup(entries), WordLookup {
    constructor(file: File) : this(parseWordLineRanges(file))

    override fun parseLine(line: ByteBuffer): WordDefinition {
        return NaspaWordListEntry.parse(line).toWordDefinition()
    }

    override fun isWord(word: Word): Boolean {
        return word is TinyWord && containsWord(word)
    }

    companion object {
        private const val spaceByte: Byte = ' '.code.toByte()

        private fun parseWordLineRanges(file: File) = LongMappedLines.create(
            file
        ) { buffer, lineStart, lineEndInclusive ->
            TinyWord.extractTinyWordFromRange(
                spaceByte,
                lineStart,
                lineEndInclusive,
                buffer::get
            ).packed
        }
    }
}