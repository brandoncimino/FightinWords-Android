package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

@Serializable
data class WordDefinition(
    val word: Word,
    val language: KnownLanguage,
    val partOfSpeech: String?,
    val definition: String,
    val isNaspaWord: Boolean
)
