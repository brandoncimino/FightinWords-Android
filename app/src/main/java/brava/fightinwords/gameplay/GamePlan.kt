package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.scoring.Ledgerman
import kotlinx.serialization.Serializable

@Serializable
data class GamePlan(
    val letterPool: LetterPool,
    val unsubmittedWordVisibility: UnsubmittedWordVisibility = UnsubmittedWordVisibility.Standard,
    val minimumWordLength: Int = 4,
    val wordLanguage: KnownLanguage = KnownLanguage.English,
    val scoreboardSorting: Ledgerman.WordSorting = Ledgerman.WordSorting.LengthFirst,
) {
}
