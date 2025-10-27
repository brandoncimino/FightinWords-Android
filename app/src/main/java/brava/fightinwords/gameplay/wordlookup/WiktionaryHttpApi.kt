package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.Word

class WiktionaryHttpApi : WordLookup, DefinitionLookup {
    override val id: WordLookup.Id = WordSource.WiktionaryHttpApi

    override fun findDefinition(word: Word): WordDefinition? {
        TODO("Not yet ported from C#")
    }

    override fun findAllDefinitions(word: Word): Sequence<WordDefinition> {
        TODO("Not yet ported from C#")
    }

    override fun isWord(word: Word): Boolean {
        TODO("Not yet ported from C#")
    }
}