package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.Typesetter.Companion.getSerializableState
import brava.fightinwords.gameplay.Umpire.Companion.getSerializableState
import brava.fightinwords.gameplay.scoring.EmployeeFactory.Companion.load
import brava.fightinwords.gameplay.scoring.Scoreboard
import brava.fightinwords.gameplay.scoring.Scoreboard.Companion.getSerializableState
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
    val scoreboard: Scoreboard,
    val onStatePossiblyChanged: () -> Unit = {}
) {
    companion object {
        fun startGame(
            gamePlan: GamePlan,
            wordPool: Sequence<WordDefinition>,
            wordScorer: WordScorer = ScrabbleScorer,
            onStatePossiblyChanged: () -> Unit = {}
        ): GameInProgress {

            return GameInProgress(
                gamePlan = gamePlan,
                umpire = Umpire(wordPool, wordScorer),
                typesetter = Typesetter(gamePlan.letterPool),
                scoreboard = Scoreboard(gamePlan.unsubmittedWordVisibility),
                onStatePossiblyChanged = onStatePossiblyChanged
            )
        }

        fun resumeGame(
            saveGameState: SaveGameState
        ): GameInProgress {
            return GameInProgress(
                saveGameState.gamePlan,
                Umpire.load(saveGameState),
                Typesetter.load(saveGameState),
                Scoreboard.load(saveGameState)
            )
        }
    }

    fun getSerializableState(): SaveGameState {
        return SaveGameState(
            gamePlan = gamePlan,
            typesetterState = typesetter.getSerializableState(),
            umpireState = umpire.getSerializableState(),
            scoreboardState = scoreboard.getSerializableState()
        )
    }

    fun submitGalley() {
        val submittedWord = typesetter.submitAndClear()
        val submissionResult = umpire.submitWord(submittedWord)
        if (submissionResult.wordState is DefinedWordState) {
            scoreboard.focusOnWord(submissionResult.wordState)
        }
        onStatePossiblyChanged()
    }
}