package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.SaveGameState.Companion.getFocusedWordDefinition
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.WordDefinition

/**
 * Manages the staff _([Typesetter], [Umpire], etc.)_.
 *
 * In most cases, staff members shouldn't talk to each other directly - instead, they should go through the [GameInProgress].
 */
class GameInProgress(
    val gamePlan: GamePlan,
    val umpire: Umpire,
    val typesetter: Typesetter,
    val focusedDefinition: FocusLens<DefinedWordState>,
    val onStatePossiblyChanged: () -> Unit = {}
) {
    companion object {
        fun startGame(
            gamePlan: GamePlan,
            wordPool: Sequence<WordDefinition>,
            wordScorer: WordScorer = ScrabbleScorer,
            onStatePossiblyChanged: () -> Unit = {}
        ): GameInProgress {
            val umpire = Umpire(
                wordPool = wordPool,
                wordScorer = wordScorer
            )

            val typesetter = Typesetter(gamePlan.letterPool)

            return GameInProgress(
                gamePlan = gamePlan,
                umpire = umpire,
                typesetter = typesetter,
                focusedDefinition = FocusLens(),
                onStatePossiblyChanged = onStatePossiblyChanged
            )
        }

        fun resumeGame(
            saveGameState: SaveGameState
        ): GameInProgress {
            val umpire = Umpire(saveGameState.wordStates)
            val typesetter = Typesetter(saveGameState.slugs)

            return GameInProgress(
                saveGameState.gamePlan,
                umpire,
                typesetter,
                FocusLens(saveGameState.getFocusedWordDefinition())
            )
        }
    }

    fun getSerializableState(): SaveGameState {
        return SaveGameState(
            gamePlan = gamePlan,
            slugs = typesetter.getSerializableState(),
            wordStates = umpire.snapshot(),
            focusedWordState = focusedDefinition.state?.map { it.word }
        )
    }

    fun submitGalley() {
        val submittedWord = typesetter.submitAndClear()
        val submissionResult = umpire.submitWord(submittedWord)
        if (submissionResult.wordState is DefinedWordState) {
            focusedDefinition.focusOn(submissionResult.wordState)
        }
        onStatePossiblyChanged()
    }
}