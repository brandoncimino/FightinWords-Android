package brava.fightinwords.gameplay.scoring

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.Word

class ScrabbleScorer : WordScorer {
    private fun getLetterScore(letter: Letter, language: KnownLanguage): Int {
        val lower = letter.character.lowercaseChar()
        return when (language) {
            KnownLanguage.English -> getEnglishLetterScore(lower)
            KnownLanguage.German -> getGermanLetterScore(lower)
            KnownLanguage.Afrikaans -> getAfrikaansLetterScore(lower)
            else -> throw IllegalArgumentException("I don't know how to play $language Scrabble!")
        }
    }

    private fun getEnglishLetterScore(lower: Char): Int {
        return when (lower) {
            'a', 'e', 'i', 'l', 'n', 'o', 'r', 's', 't', 'u' -> 1
            'd', 'g' -> 2
            'b', 'c', 'm', 'p' -> 3
            'f', 'h', 'v', 'w', 'y' -> 4
            'k' -> 5
            'j', 'x' -> 8
            'q', 'z' -> 10
            else -> rejectChar(lower, KnownLanguage.English)
        }
    }

    private fun getGermanLetterScore(lower: Char): Int {
        return when (lower) {
            'e', 'n', 's', 'i', 'r', 't', 'u', 'a', 'd' -> 1
            'h', 'g', 'l', 'o' -> 2
            'm', 'b', 'w', 'z' -> 3
            'c', 'f', 'k', 'p' -> 4
            'ä', 'j', 'ü', 'v' -> 6
            'ö', 'x' -> 8
            'q', 'y' -> 10
            else -> rejectChar(lower, KnownLanguage.German)
        };
    }

    private fun getAfrikaansLetterScore(lower: Char): Int {
        return when (lower) {
            'e', 'a', 'i', 'o', 'n', 'r', 't', 'l', 's', 'u' -> 1
            'd', 'g' -> 2
            'b', 'c', 'm', 'p' -> 3
            'f', 'h', 'v', 'w', 'y' -> 4
            'k' -> 5
            'j', 'x' -> 8
            'q', 'z' -> 10
            else -> rejectChar(lower, KnownLanguage.Afrikaans)
        }
    }

    private fun rejectChar(lower: Char, language: KnownLanguage): Nothing {
        throw IllegalArgumentException("`$lower` is not a valid letter in $language Scrabble!")
    }

    override fun getScore(word: Word, language: KnownLanguage): Int {
        return word.sumOf { getLetterScore(it, language) }
    }

}