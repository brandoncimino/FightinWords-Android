package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.Word

fun interface DefinitionLookup {
    fun findDefinition(word: Word): WordDefinition?
}