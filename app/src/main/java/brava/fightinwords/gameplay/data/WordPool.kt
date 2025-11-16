package brava.fightinwords.gameplay.data

import brava.fightinwords.gameplay.wordlookup.WordList
import brava.fightinwords.gameplay.wordlookup.findConstructibleWords

/**
 * A [letterPool] and the [words] it [LetterPool.canConstruct].
 */
class WordPool private constructor(
    val letterPool: LetterPool,
    val words: Set<Word>,
) {
    init {
        require(words.isNotEmpty()) { "You cannot play with an empty ${this::class.simpleName}!" }
    }

    constructor(
        letterPool: LetterPool,
        wordList: WordList,
        wordLengthRange: IntRange,
    ) : this(
        letterPool,
        wordList.findConstructibleWords(letterPool, wordLengthRange.first, wordLengthRange.last)
    )

    fun contains(word: Word): Boolean = words.contains(word)
}