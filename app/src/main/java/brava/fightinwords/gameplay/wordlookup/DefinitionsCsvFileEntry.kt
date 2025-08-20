package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.Substring

data class DefinitionsCsvFileEntry(
    val rawEntry: Substring,
    val wordLength: Int,
) {
    val word get() = rawEntry.subSequence(0, wordLength)
}