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
    constructor(
        letterPool: LetterPool,
        wordList: WordList,
        minimumWordLength: Int,
        maximumWordLength: Int = minimumWordLength + letterPool.size,
    ) : this(
        letterPool,
        wordList.findConstructibleWords(letterPool, minimumWordLength, maximumWordLength)
    )

    fun contains(word: Word): Boolean = words.contains(word)
}