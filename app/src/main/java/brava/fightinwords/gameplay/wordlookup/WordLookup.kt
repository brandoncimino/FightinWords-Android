package brava.fightinwords.gameplay.wordlookup

import kotlin.random.Random

interface WordLookup {
    abstract fun isWord(word: String): Boolean;

    abstract fun findRandomWord(desiredLength: Int, random: Random): Result<String>;
}