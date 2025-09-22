package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.utf8
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word

fun DefinitionsCsvLookup(
    bytes: ByteSlice,
) = DefinitionsCsvLookup(
    DefinitionsCsvLookup.parseWordLineRanges(bytes),
    bytes
)

class DefinitionsCsvLookup(
    val index: ShortlexWordListIndex,
    private val bytes: ByteSlice,
    private val language: KnownLanguage = KnownLanguage.English,
) : DefinitionLookup, WordLookup {
    override fun findDefinition(word: Word): WordDefinition? {
        return when (word) {
            is TinyWord -> findDefinition(word)
            else        -> null
        }
    }

    fun findDefinition(tinyWord: TinyWord): WordDefinition? {
        val range = index.entries.findRange(tinyWord)

        if (range.isEmpty) {
            return null
        }

        val rawEntry = bytes.slice(range)
        return WordLookupHelpers.parseDefinitionCsvLine(
            // TODO: this is super gross
            rawEntry.toByteBuffer().utf8(),
            language
        )
    }

    override fun isWord(word: Word): Boolean {
        return word is TinyWord && index.entries.containsWord(word)
    }

    companion object {
        private val commaByte: Byte = ','.code.toByte()
        internal fun parseWordLineRanges(
            bytes: ByteSlice,
        ) = ShortlexWordListIndex.build(
            bytes,
            { lineStart, lineEndInclusive ->
                TinyWord.extractTinyWordFromRange(
                    commaByte,
                    lineStart,
                    lineEndInclusive,
                    bytes::get
                )
            }
        )
    }
}