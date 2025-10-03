package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import java.io.InputStream
import java.nio.CharBuffer
import kotlin.math.min

internal object WordLookupHelpers {
    fun parseConstructibleWords(
        csvStream: InputStream,
        language: KnownLanguage,
        letterPool: LetterPool,
        wordLengthRange: IntRange,
        csvDelimiter: Char = ',',
    ): Sequence<WordDefinition> {
        val buffer = CharArray(min(letterPool.size, wordLengthRange.endInclusive))

        return csvStream.bufferedReader(Charsets.UTF_8)
            .lineSequence()
            .filter {
                val wordLength = letterPool.canConstructBuffered(
                    it.asSequence().takeWhile { c -> c != csvDelimiter }, buffer
                )

                return@filter wordLength in wordLengthRange
            }
            .map { parseDefinitionCsvLine(CharBuffer.wrap(it), language) }
    }

    /**
     * @return the length of the [word], or -1 if I can't construct it
     */
    @Deprecated("Need to move to the nice `canConstruct`.")
    private fun LetterPool.canConstructBuffered(
        word: Sequence<Char>, buffer: CharArray,
    ): Int {
        for (i in 0..size) {
            buffer[i] = codePoints[i].toChar()
        }

        var length = 0

        for (letter in word) {
            length += 1
            assert(letter != Char.MIN_VALUE)

            val matchIndex = buffer.indexOf(letter)

            if (matchIndex < 0) {
                return -1
            } else {
                buffer[matchIndex] = Char.MIN_VALUE
            }
        }

        return length
    }

    fun parseDefinitionCsvLine(line: CharBuffer, language: KnownLanguage): WordDefinition {
        val cells = line.split(',', limit = 4)
        assert(cells.size == 4)

        val (word, partOfSpeech, isNaspaWord, definition) = cells

        return WordDefinition(
            word = word.trim { it.isLetter() == false }.toWord(),
            language = language,
            partOfSpeech = partOfSpeech,
            definition = definition,
            source = WordSource.DefinitionsCsv
        )
    }
}