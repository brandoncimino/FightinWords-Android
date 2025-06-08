package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology
import kotlin.random.Random

data object LetterPoolGenerators {
    fun randomEnglishPool(count: Int, minimumVowels: Int, random: Random): List<Char> {
        assert(minimumVowels <= count)

        val initialLetters = (0 until count)
            .map { randomEnglishLetter(random) }

        return ensureMinimumEnglishVowels(initialLetters, minimumVowels, random)
    }

    fun ensureMinimumEnglishVowels(pool: List<Char>, minimumVowels: Int, random: Random): List<Char> {
        val initialVowelCount = pool.count {
            it.englishPhonology == Phonology.Vowel
        }

        val bonusVowelsNeeded = minimumVowels - initialVowelCount;

        if (bonusVowelsNeeded <= 0) {
            return pool
        }

        val mutablePool = pool.toMutableList()

        repeat(bonusVowelsNeeded) {
            val firstConsonant = pool.indexOfFirst { it.englishPhonology == Phonology.Consonant }
            mutablePool[firstConsonant] = randomEnglishVowel(random)
        }

        return mutablePool.toList()
    }

    fun randomEnglishLetter(random: Random): Char {
        return random.nextInt(
            'a'.code,
            'z'.code
        ).toChar()
    }

    fun randomEnglishVowel(random: Random): Char {
        return Phonology.englishVowels.random(random)
    }
}