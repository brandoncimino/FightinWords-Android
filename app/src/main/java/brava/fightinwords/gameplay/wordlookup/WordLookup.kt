package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import kotlin.random.Random
import kotlin.random.nextInt

fun interface WordLookup {
    fun isWord(word: Word): Boolean

    fun findAllPossibleWords(
        letterPool: LetterPool,
        wordLength: Int
    ): List<Word> {
        throw UnsupportedOperationException()
    }

    fun findAllPossibleWords(
        letterPool: LetterPool,
        wordLengthRange: IntRange,
    ) {
        throw UnsupportedOperationException()
    }

    fun findRandomWord(desiredLength: Int, random: Random): Result<Word> {
        return Result.failure(UnsupportedOperationException("${this} cannot generate random words."))
    }
}

/**
 * A [WordLookup] that supports ordered, random access.
 *
 * For example, the [NaspaWordList] is known ahead-of-time with a defined order, so it can be access efficiently.
 * Meanwhile, looking up words via an HTTP API like Wiktionary's would not be.
 */
sealed interface WordList : WordLookup {
    val wordCount: Int

    fun getWordByIndex(wordIndex: Int): Word
}

/**
 * A [WordList] that is known to be in [shortlex order](https://en.wikipedia.org/wiki/Shortlex_order).
 */
sealed interface ShortlexWordList : WordList {
    fun getCountOfWordsWithLength(wordLength: Int): Int

    fun getRangeOfWordsWithLength(wordLength: Int): IntRange {
        val start = getStartOfWordsWithLength(wordLength)
        val length = getCountOfWordsWithLength(wordLength)
        return start until (start + length)
    }

    fun getRandomWord(wordLength: Int, random: Random): Word {
        val wordLengthRange = getRangeOfWordsWithLength(wordLength)
        val wordOffset = random.nextInt(wordLengthRange)
        return getWordByIndex(wordLengthRange.start + wordOffset)
    }
}

private fun ShortlexWordList.getStartOfWordsWithLength(wordLength: Int): Int {
    var start = 0
    for (i in 0 until wordLength) {
        start += getCountOfWordsWithLength(i)
    }
    return start
}