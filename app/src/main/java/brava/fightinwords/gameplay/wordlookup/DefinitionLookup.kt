package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.Word

interface DefinitionLookup : WordLookup {
    fun findDefinition(word: Word): WordDefinition?

    companion object {
        fun DefinitionLookup.requireDefinition(word: Word): WordDefinition {
            return checkNotNull(findDefinition(word)) {
                "Couldn't find a definition for `$word` in $this!"
            }
        }
    }
}