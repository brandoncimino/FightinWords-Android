package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.random.Random

class LetterPoolGeneratorsTest {
    @Test
    fun ensureMinimumEnglishVowels() {
        val tooFewVowels = "abc".toList()
        val minimumVowels = 2

        val with2Vowels = LetterPoolGenerators.ensureMinimumEnglishVowels(tooFewVowels, minimumVowels, Random(1))

        val actualVowelCount = with2Vowels.count() { it.englishPhonology == Phonology.Vowel }
        Assertions.assertThat(actualVowelCount)
            .isEqualTo(minimumVowels)
    }

    @Test
    fun noBonusVowelsNeeded() {
        val hasEnoughVowels = "aaa".toList()
        val minimumVowels = 2;

        val with2Vowels = LetterPoolGenerators.ensureMinimumEnglishVowels(hasEnoughVowels, minimumVowels, Random(1))

        Assertions.assertThat(with2Vowels)
            .isEqualTo(hasEnoughVowels)
    }
}