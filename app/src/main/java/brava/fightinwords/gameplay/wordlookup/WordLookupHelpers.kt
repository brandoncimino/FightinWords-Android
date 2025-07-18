package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import java.io.InputStream

internal object WordLookupHelpers {
    fun parseConstructibleWords(
        csvStream: InputStream,
        language: KnownLanguage,
        letterPool: LetterPool,
        csvDelimiter: Char = ','
    ): Sequence<WordDefinition> {
        val buffer = CharArray(letterPool.size)

        return csvStream.bufferedReader(Charsets.UTF_8)
            .lineSequence()
            .filter {
                letterPool.canConstructInternal(
                    it.asSequence().takeWhile { c -> c != csvDelimiter },
                    buffer
                )
            }
            .map { parseDefinitionCsvLine(it, language) }
    }

    fun parseDefinitionsFile(csvStream: InputStream, language: KnownLanguage): Sequence<WordDefinition> {
        return csvStream.bufferedReader(Charsets.UTF_8)
            .lineSequence()
            .map { parseDefinitionCsvLine(it, language) }
    }

    private fun parseDefinitionCsvLine(line: String, language: KnownLanguage): WordDefinition {
        val cells = line.split(',', limit = 4)
        assert(cells.size == 4)

        val (word, partOfSpeech, isNaspaWord, definition) = cells

        return WordDefinition(
            word = word.trim { it.isLetter() == false }.toWord(),
            language = language,
            partOfSpeech = partOfSpeech,
            definition = definition,
            isNaspaWord = when (isNaspaWord) {
                "0" -> false
                "1" -> true
                else -> throw IllegalArgumentException("Unknown value for `isNaspaWord`: `$isNaspaWord`. Must be 0 (false) or 1 (true).")
            }
        )
    }
}