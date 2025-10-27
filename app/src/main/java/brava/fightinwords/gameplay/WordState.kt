package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import kotlinx.serialization.Serializable

// TODO: Using polymorphic deserialization adds a `type` field to each JSON with a value of the full type name, which bloats the total save state a TON.
//       See if there's a way to use either:
//           - The `simpleName`
//           - A "type identifier" field, like a enum `StateEnum` enum
//       Words case scenario, I can probably wrap them in a `SerializableWordState(StateEnum, WordState)`
@Serializable
sealed interface WordState {
    val word: Word
}

@Serializable
sealed interface DefinedWordState : WordState {
    val wordDefinition: WordDefinition
    override val word get() = wordDefinition.word
    val category: WordCategory
}

@Serializable
data class Unplayed(
    override val wordDefinition: WordDefinition,
    override val category: WordCategory,
) : DefinedWordState

@Serializable
data class Rejected(override val word: Word) : WordState

@Serializable
data class Accepted(
    override val wordDefinition: WordDefinition,
    override val category: WordCategory,
    val points: Int,
) : DefinedWordState