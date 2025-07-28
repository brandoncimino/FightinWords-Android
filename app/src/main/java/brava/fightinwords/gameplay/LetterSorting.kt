package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology

enum class LetterSorting(val comparator: Comparator<Char>) : Comparator<Char> by comparator {
    Alphabetical(Comparator.naturalOrder()),
    Phonological(Comparator.comparingInt { it.englishPhonology.ordinal });
}