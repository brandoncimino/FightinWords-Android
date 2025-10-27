@file:Suppress("REDUNDANT_ELSE_IN_WHEN")

package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.random.nextInt

sealed interface WordLookup {
    fun isWord(word: Word): Boolean
    val id: WordLookup.Id

    sealed interface Id
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

    fun getCountOfWordsWithLength(wordLength: Int): Int

    val wordLengthRange: IntRange

    override val id: WordList.Id

    @Serializable
    sealed interface Id : WordLookup.Id
}

inline fun WordList.forEachWord(action: (Word) -> Unit) {
    for (i in 0 until wordCount) {
        val word = getWordByIndex(i)
        action(word)
    }
}

val WordList.indices inline get() = 0..wordCount

/**
 * A [WordList] that is known to be in [shortlex order](https://en.wikipedia.org/wiki/Shortlex_order).
 */
sealed interface ShortlexWordList : WordList

private fun ShortlexWordList.getRandomWord(wordLength: Int, random: Random): Word {
    val wordLengthRange = getRangeOfWordsWithLength(wordLength)
    val wordOffset = random.nextInt(wordLengthRange)
    return getWordByIndex(wordLengthRange.start + wordOffset)
}

private fun ShortlexWordList.getStartOfWordsWithLength(wordLength: Int): Int {
    var start = 0
    for (i in 0 until wordLength) {
        start += getCountOfWordsWithLength(i)
    }
    return start
}

fun ShortlexWordList.getRangeOfWordsWithLength(
    minimumWordLength: Int,
    maximumWordLength: Int,
): IntRange {
    val rangeStart = getStartOfWordsWithLength(minimumWordLength)

    var rangeEndExclusive = rangeStart

    for (i in minimumWordLength..maximumWordLength) {
        rangeEndExclusive += getCountOfWordsWithLength(i)
    }

    return rangeStart until rangeEndExclusive
}

private fun ShortlexWordList.getRangeOfWordsWithLength(wordLength: Int): IntRange {
    val start = getStartOfWordsWithLength(wordLength)
    val length = getCountOfWordsWithLength(wordLength)
    return start until (start + length)
}

inline fun WordList.forEachWordWithLength(
    minimumWordLength: Int,
    maximumWordLength: Int,
    action: (Word) -> Unit,
) {
    return when (this) {
        is ShortlexWordList ->
            for (i in getRangeOfWordsWithLength(minimumWordLength, maximumWordLength)) {
                action(getWordByIndex(i))
            }

        else                -> for (i in 0..wordCount) {
            val word = getWordByIndex(i)
            if (word.length in minimumWordLength..maximumWordLength) {
                action(word)
            }
        }
    }
}

fun WordList.findConstructibleWords(
    letterPool: LetterPool,
    minimumWordLength: Int,
    maximumWordLength: Int = minimumWordLength + letterPool.size,
): Set<Word> {
    return when (this) {
        is ShortlexWordList -> {
            getRangeOfWordsWithLength(minimumWordLength, maximumWordLength)
                .asSequence()
                .map { getWordByIndex(it) }
                .filter { letterPool.canConstruct(it) }
                .toSet()
        }

        else                -> buildSet {
            forEachWord { letterPool.canConstruct(it) }
        }
    }
}

fun WordList.getRandomWord(desiredLength: Int, random: Random = Random): Word {
    return when (this) {
        is ShortlexWordList -> getRandomWord(desiredLength, random)
        else                ->
            indices
                .asSequence()
                .map { getWordByIndex(it) }
                .filter { it.length == desiredLength }
                .elementAt(Random.nextInt(getCountOfWordsWithLength(desiredLength)))
    }
}