package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.scoring.Ledgerman
import brava.fightinwords.gameplay.scoring.WordScoringStrategy
import brava.fightinwords.gameplay.wordlookup.WordList
import brava.fightinwords.gameplay.wordlookup.WordLookup
import brava.fightinwords.gameplay.wordlookup.WordSource
import kotlinx.serialization.Serializable
import kotlin.math.min

@Serializable
data class GamePlan(
    val letterPool: Word,
    val unsubmittedWordVisibility: UnsubmittedWordVisibility = UnsubmittedWordVisibility.Standard,
    val minimumWordLength: Int = 3,
    /**
     * The maximum [Word.length] you want to play with _(if it's different from [letterPool]`.length`)_
     */
    val maximumWordLengthOverride: Int? = null,
    val wordLanguage: KnownLanguage = KnownLanguage.English,
    val scoreboardSorting: Ledgerman.WordSorting = Ledgerman.WordSorting.LengthFirst,

    /**
     * The [brava.fightinwords.gameplay.wordlookup.WordList] that defines my [WordCategory.Core] words.
     */
    val coreWordList: WordList.Id = WordSource.NaspaWordList2023,

    /**
     * Places where we can check for [WordCategory.Bonus] words.
     */
    val bonusWordLookups: List<WordLookup.Id> = listOf(),

    val scoringStrategy: WordScoringStrategy = WordScoringStrategy.Scrabble,
) {
    companion object {
        val GamePlan.wordLengthRange: IntRange
            get() {
                val maxLength = when (maximumWordLengthOverride) {
                    null -> letterPool.length
                    else -> min(maximumWordLengthOverride, letterPool.length)
                }

                return minimumWordLength..maxLength
            }
    }
}
