package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.TinyWord

enum class Phonology {
    Vowel,
    SemiVowel,
    Consonant,
    Unknown
    ;

    companion object {
        const val englishVowels = "aeiou";
        const val englishSemivowels = "y";

        val Char.englishPhonology: Phonology
            get() {
                if (englishVowels.contains(this.lowercase())) {
                    return Vowel
                }

                if (englishSemivowels.contains(this.lowercase())) {
                    return SemiVowel
                }

                return Consonant;
            }

        val Letter.englishPhonology: Phonology
            get() {
                return when (codePoint) {
                    'a'.code, 'e'.code, 'i'.code, 'o'.code, 'u'.code -> Vowel
                    'y'.code                                         -> SemiVowel
                    else                                             -> Consonant
                }
            }
    }
}