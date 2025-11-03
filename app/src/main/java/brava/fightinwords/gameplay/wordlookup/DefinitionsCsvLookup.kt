package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.sequenceOfNotNull
import brava.fightinwords.botlin.skipUtf8ByteOrderMark
import brava.fightinwords.botlin.utf8
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import java.nio.CharBuffer

fun DefinitionsCsvLookup(
    bytes: ByteSlice,
): DefinitionsCsvLookup {
    val cleanBytes = bytes.skipUtf8ByteOrderMark()
    return DefinitionsCsvLookup(
        DefinitionsCsvLookup.parseWordLineRanges(cleanBytes),
        cleanBytes
    )
}

class DefinitionsCsvLookup(
    val index: WordListIndex,
    private val bytes: ByteSlice,
    private val language: KnownLanguage = KnownLanguage.English,
) : DefinitionLookup, WordList {
    override val id: WordList.Id
        get() = WordSource.DefinitionsCsv

    override fun findDefinition(word: Word): WordDefinition? {
        return when (word) {
            is TinyWord -> findDefinition(word)
            else        -> null
        }
    }

    override fun findAllDefinitions(word: Word): Sequence<WordDefinition> {
        return sequenceOfNotNull(findDefinition(word))
    }

    fun findDefinition(tinyWord: TinyWord): WordDefinition? {
        val range = index.findWordRange(tinyWord)

        if (range.isEmpty) {
            return null
        }

        val rawEntry = bytes.slice(range)
        return parseDefinitionCsvLine(
            // TODO: this is super gross
            rawEntry.toByteBuffer().utf8(),
            language
        )
    }

    override fun isWord(word: Word): Boolean {
        return word is TinyWord && index.findWordIndex(word) >= 0
    }

    override fun getCountOfWordsWithLength(wordLength: Int): Int {
        return index.wordLengthCounts[wordLength]
    }

    override val wordLengthRange: IntRange
        get() = index.wordLengthRange
    override val wordCount: Int
        get() = index.wordCount

    override fun getWordByIndex(wordIndex: Int): Word {
        TODO("Not yet implemented")
    }

    companion object {
        private val commaByte: Byte = ','.code.toByte()
        internal fun parseWordLineRanges(
            bytes: ByteSlice,
        ) = WordListIndex.build(bytes, stopOnEmptyWord = false) { lineStart, lineEndInclusive ->
            TinyWord.extractTinyWordFromStart(
                commaByte,
                lineStart,
                lineEndInclusive,
                bytes::get,
                TinyWord.Companion.LongWordHandling.Skip,
            )
        }

        fun parseDefinitionCsvLine(line: CharBuffer, language: KnownLanguage): WordDefinition {
            val cells = line.split(',', limit = 4)
            assert(cells.size == 4)

            val (word, partOfSpeech, isNaspaWord, definition) = cells

            return WordDefinition(
                word = word.trim { it.isLetter() == false }.toWord(),
                language = language,
                partOfSpeech = PartOfSpeech.tryParse(partOfSpeech),
                definition = definition,
                source = WordSource.DefinitionsCsv
            )
        }
    }
}