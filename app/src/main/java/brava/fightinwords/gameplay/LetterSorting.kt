package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology
import brava.fightinwords.gameplay.data.Letter

enum class LetterSorting(val comparator: Comparator<Letter>) : Comparator<Letter> by comparator {
    Alphabetical(Comparator.naturalOrder()),
    Phonological(Comparator.comparingInt { it.englishPhonology.ordinal });
}