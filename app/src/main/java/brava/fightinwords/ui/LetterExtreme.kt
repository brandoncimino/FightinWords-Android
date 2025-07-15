package brava.fightinwords

import brava.fightinwords.gameplay.Phonology
import brava.fightinwords.gameplay.Phonology.*
import org.jetbrains.annotations.ApiStatus
import java.text.Normalizer
import java.util.*

@ApiStatus.Experimental
data class LetterExtreme(val raw: String, val locale: Locale = Locale.ROOT) : Comparable<LetterExtreme> {
    val decomposed: String
        get() {
            return Normalizer.normalize(raw, Normalizer.Form.NFD)
        }

    val platonic: String
        get() {
            val lowercase = decomposed.lowercase(locale)
            val sb = StringBuilder();
            lowercase.codePoints()
                .filter {
                    isNotDiacritic(it)
                }
                .forEach { sb.appendCodePoint(it) }

            return sb.toString()
        }

    private fun isNotDiacritic(codePoint: Int): Boolean {
        return Character.UnicodeBlock.of(codePoint) != Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS
    }

    val phonology: Phonology
        get() {
            if (locale.language == "" || locale.language == "en") {
                return when (platonic.length) {
                    1 -> getEnglishPhonology(platonic[0])
                    else -> Phonology.Unknown
                }
            }

            return Phonology.Unknown;
        }

    override fun compareTo(other: LetterExtreme): Int {
        return alphabeticalComparator.compare(this, other)
    }

    companion object {
        val alphabeticalComparator: Comparator<LetterExtreme> =
            Comparator.comparing<LetterExtreme, String> { it.platonic }
            .thenBy { it.decomposed }
            .thenBy { it.raw }

        val phonologicalComparator: Comparator<LetterExtreme> =
            Comparator.comparing<LetterExtreme, Phonology> { it.phonology }
            .then(alphabeticalComparator)

        private fun getEnglishPhonology(letter: Char): Phonology {
            return when (letter) {
                'a', 'e', 'i', 'o', 'u' -> Vowel;
                'y' -> SemiVowel;
                else -> Consonant
            }
        }
    }
}