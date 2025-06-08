package brava.fightinwords.gameplay

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
    }
}