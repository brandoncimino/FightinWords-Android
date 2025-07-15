package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology

enum class LetterSorting(val comparator: Comparator<Char>) : Comparator<Char> {
    Alphabetical(Comparator.naturalOrder()),
    Phonological(Comparator.comparing { it.englishPhonology });

    override fun compare(o1: Char, o2: Char): Int {
        return comparator.compare(o1, o2)
    }
}