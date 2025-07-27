package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import kotlinx.serialization.Serializable

@Serializable
sealed interface WordState {
    val word: Word
}

@Serializable
sealed interface DefinedWordState : WordState {
    val wordDefinition: WordDefinition
    override val word get() = wordDefinition.word
}

@Serializable
data class Unplayed(override val wordDefinition: WordDefinition) : DefinedWordState

@Serializable
data class Rejected(override val word: Word) : WordState

@Serializable
data class Accepted(override val wordDefinition: WordDefinition, val points: Int) : DefinedWordState