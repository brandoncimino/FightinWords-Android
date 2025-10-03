package brava.fightinwords.gameplay.wordlookup

import kotlinx.serialization.Serializable

object WordSource {
    @Serializable
    object NaspaWordList2023 : WordList.Id, DefinitionLookup.Id

    @Serializable
    object DefinitionsCsv : WordList.Id, DefinitionLookup.Id

    @Serializable
    object WiktionaryHttpApi : WordLookup.Id, DefinitionLookup.Id
}