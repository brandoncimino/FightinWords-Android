package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.LetterPool
import kotlinx.serialization.Serializable

@Serializable
data class GamePlan(
    val letterPool: LetterPool,
    val unsubmittedWordVisibility: UnsubmittedWordVisibility = UnsubmittedWordVisibility.Standard,
    val minimumWordLength: Int = 4,
    val wordLanguage: KnownLanguage = KnownLanguage.English,
) {
    fun isVisible(wordState: WordState): Boolean {
        return when (wordState) {
            is Accepted -> true
            else ->
                when (unsubmittedWordVisibility) {
                    UnsubmittedWordVisibility.None -> false
                    UnsubmittedWordVisibility.Standard -> wordState is DefinedWordState && wordState.wordDefinition.isNaspaWord
                    UnsubmittedWordVisibility.All -> true
                }
        }
    }
}
