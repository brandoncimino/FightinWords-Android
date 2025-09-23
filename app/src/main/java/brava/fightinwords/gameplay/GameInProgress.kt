package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.Typesetter.Companion.getSerializableState
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.hr.EmployeeFactory.Companion.load
import brava.fightinwords.gameplay.scoring.Ledgerman
import brava.fightinwords.gameplay.scoring.Ledgerman.Companion.getSerializableState
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup.Companion.requireDefinition

/**
 * Manages the staff _([Typesetter], [Umpire], etc.)_.
 *
 * In most cases, staff members shouldn't talk to each other directly - instead, they should go through the [GameInProgress].
 */
class GameInProgress(
    val gamePlan: GamePlan,
    val typesetter: Typesetter,
    val ledgerman: Ledgerman,
    val definitionLookup: DefinitionLookup,
    val onStatePossiblyChanged: () -> Unit = {},
) {
    inline val umpire inline get() = ledgerman.umpire

    companion object {
        inline val GamePlan.wordLengthRange
            inline get() = minimumWordLength..letterPool.length

        fun startGame(
            gamePlan: GamePlan,
            wordPool: Sequence<Word>,
            wordScorer: WordScorer = ScrabbleScorer,
            definitionLookup: DefinitionLookup,
            onStatePossiblyChanged: () -> Unit = {},
        ): GameInProgress {
            return GameInProgress(
                gamePlan = gamePlan,
                typesetter = Typesetter(gamePlan.letterPool),
                ledgerman = Ledgerman(
                    unsubmittedWordVisibility = gamePlan.unsubmittedWordVisibility,
                    wordLengthRange = gamePlan.wordLengthRange,
                    umpire = Umpire(wordPool, wordScorer)
                ),
                definitionLookup = definitionLookup,
                onStatePossiblyChanged = onStatePossiblyChanged
            )
        }

        fun resumeGame(
            saveGameState: SaveGameState,
            definitionLookup: DefinitionLookup,
        ): GameInProgress {
            return GameInProgress(
                saveGameState.gamePlan,
                Typesetter.load(saveGameState),
                Ledgerman.load(saveGameState),
                definitionLookup
            )
        }
    }

    fun getSerializableState(): SaveGameState {
        return SaveGameState(
            gamePlan = gamePlan,
            typesetterState = typesetter.getSerializableState(),
            ledgermanState = ledgerman.getSerializableState()
        )
    }

    fun submitGalley(): SubmissionResult {
        val submittedWord = typesetter.submitAndClear()
        val submissionResult = umpire.submitWord(submittedWord)

        if (submissionResult is SubmissionResult.Accepted) {
            val definition = definitionLookup.requireDefinition(submissionResult.word)
            val wordState = Accepted(definition, submissionResult.points)
            ledgerman.focusOnWord(wordState)
        }

        onStatePossiblyChanged()
        return submissionResult
    }
}