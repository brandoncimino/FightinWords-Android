package brava.fightinwords.gameplay.scoring

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.Letter.Companion.describe
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.indices

sealed class ScrabbleScorer : WordScorer {
    companion object Default : ScrabbleScorer()

    private fun getLetterScore(letter: Letter, language: KnownLanguage): Int {
        return when (language) {
            KnownLanguage.English -> getEnglishLetterScore(letter)
            KnownLanguage.German -> getGermanLetterScore(letter)
            KnownLanguage.Afrikaans -> getAfrikaansLetterScore(letter)
            else -> throw IllegalArgumentException("I don't know how to play $language Scrabble!")
        }
    }

    private fun getEnglishLetterScore(letter: Letter): Int {
        return when (letter.codePoint) {
            'a'.code, 'e'.code, 'i'.code, 'l'.code, 'n'.code, 'o'.code, 'r'.code, 's'.code, 't'.code, 'u'.code -> 1
            'd'.code, 'g'.code                                                                                 -> 2
            'b'.code, 'c'.code, 'm'.code, 'p'.code                                                             -> 3
            'f'.code, 'h'.code, 'v'.code, 'w'.code, 'y'.code                                                   -> 4
            'k'.code                                                                                           -> 5
            'j'.code, 'x'.code                                                                                 -> 8
            'q'.code, 'z'.code                                                                                 -> 10
            else                                                                                               -> rejectLetter(
                letter,
                KnownLanguage.English
            )
        }
    }

    private fun getGermanLetterScore(letter: Letter): Int {
        return when (letter.codePoint) {
            'e'.code, 'n'.code, 's'.code, 'i'.code, 'r'.code, 't'.code, 'u'.code, 'a'.code, 'd'.code -> 1
            'h'.code, 'g'.code, 'l'.code, 'o'.code                                                   -> 2
            'm'.code, 'b'.code, 'w'.code, 'z'.code                                                   -> 3
            'c'.code, 'f'.code, 'k'.code, 'p'.code                                                   -> 4
            'ä'.code, 'j'.code, 'ü'.code, 'v'.code                                                   -> 6
            'ö'.code, 'x'.code                                                                       -> 8
            'q'.code, 'y'.code                                                                       -> 10
            else                                                                                     -> rejectLetter(
                letter,
                KnownLanguage.German
            )
        };
    }

    private fun getAfrikaansLetterScore(letter: Letter): Int {
        return when (letter.codePoint) {
            'e'.code, 'a'.code, 'i'.code, 'o'.code, 'n'.code, 'r'.code, 't'.code, 'l'.code, 's'.code, 'u'.code -> 1
            'd'.code, 'g'.code                                                                                 -> 2
            'b'.code, 'c'.code, 'm'.code, 'p'.code                                                             -> 3
            'f'.code, 'h'.code, 'v'.code, 'w'.code, 'y'.code                                                   -> 4
            'k'.code                                                                                           -> 5
            'j'.code, 'x'.code                                                                                 -> 8
            'q'.code, 'z'.code                                                                                 -> 10
            else                                                                                               -> rejectLetter(
                letter,
                KnownLanguage.Afrikaans
            )
        }
    }

    private fun rejectLetter(letter: Letter, language: KnownLanguage): Nothing {
        throw IllegalArgumentException("${letter.describe()} is not a valid letter in $language Scrabble!")
    }

    override fun getScore(word: Word, language: KnownLanguage): Int {
        var score = 0
        for (i in word.indices) {
            score += getLetterScore(word[i], language)
        }
        return score
    }

}