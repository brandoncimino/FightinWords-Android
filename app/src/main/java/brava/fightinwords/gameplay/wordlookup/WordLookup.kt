package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import kotlin.random.Random

fun interface WordLookup {
    fun isWord(word: Word): Boolean

    fun findAllPossibleWords(
        letterPool: LetterPool,
        wordLength: Int
    ): List<Word> {
        throw UnsupportedOperationException()
    }

    fun findRandomWord(desiredLength: Int, random: Random): Result<Word> {
        return Result.failure(UnsupportedOperationException("${this} cannot generate random words."))
    }
}