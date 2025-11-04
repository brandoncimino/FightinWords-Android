package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.GamePlan.Companion.wordLengthRange
import brava.fightinwords.gameplay.Typesetter.Companion.getSerializableState
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.WordPool
import brava.fightinwords.gameplay.hr.EmployeeFactory
import brava.fightinwords.gameplay.hr.EmployeeFactory.Companion.load
import brava.fightinwords.gameplay.scoring.Ledgerman
import brava.fightinwords.gameplay.scoring.Ledgerman.Companion.getSerializableState
import brava.fightinwords.gameplay.wordlookup.Factotum.Companion.getFactotum
import brava.fightinwords.gameplay.wordlookup.WordKey
import brava.fightinwords.gameplay.wordlookup.WordSourceLoader

/**
 * Manages the staff _([Typesetter], [Arbiter], etc.)_.
 *
 * In most cases, staff members shouldn't talk to each other directly - instead, they should go through the [GameInProgress].
 */
class GameInProgress(
    val sharedResources: EmployeeFactory.SharedResources,
    val typesetter: Typesetter,
    val ledgerman: Ledgerman,
    val onStatePossiblyChanged: () -> Unit = {},
) {
    inline val arbiter inline get() = ledgerman.arbiter

    companion object {
        fun startGame(
            wordSourceLoader: WordSourceLoader,
            gamePlan: GamePlan,
            onStatePossiblyChanged: () -> Unit = {},
        ): GameInProgress {
            val coreWordList = wordSourceLoader.getWordList(gamePlan.coreWordList)
            val coreWordPool = WordPool(
                letterPool = LetterPool(gamePlan.letterPool),
                wordList = coreWordList,
                wordLengthRange = gamePlan.wordLengthRange
            )

            val factotum = wordSourceLoader.getFactotum(gamePlan)
            val sharedResources = EmployeeFactory.SharedResources(wordSourceLoader, gamePlan)

            return GameInProgress(
                sharedResources = sharedResources,
                typesetter = Typesetter(gamePlan.letterPool),
                ledgerman = Ledgerman(
                    unsubmittedWordVisibility = gamePlan.unsubmittedWordVisibility,
                    wordLengthRange = gamePlan.wordLengthRange,
                    arbiter = Arbiter(factotum),
                    coreWordPool = coreWordPool,
                    sharedResources = sharedResources
                ),
                onStatePossiblyChanged = onStatePossiblyChanged
            )
        }

        fun resumeGame(
            wordSourceLoader: WordSourceLoader,
            saveGameState: SaveGameState,
        ): GameInProgress {
            val sharedResources = EmployeeFactory.SharedResources(
                wordSourceLoader,
                saveGameState.gamePlan
            )
            return GameInProgress(
                sharedResources,
                Typesetter.load(saveGameState, sharedResources),
                Ledgerman.load(saveGameState, sharedResources),
            )
        }
    }

    fun getSerializableState(): SaveGameState {
        return SaveGameState(
            gamePlan = sharedResources.gamePlan,
            typesetterState = typesetter.getSerializableState(),
            ledgermanState = ledgerman.getSerializableState()
        )
    }

    fun submitGalley(): SubmissionResult {
        val submittedWord = typesetter.submitAndClear()
        val submissionResult = arbiter.submitWord(submittedWord)

        if (submissionResult is SubmissionResult.Accepted) {
            ledgerman.focusOnWord(WordKey(submittedWord))
        }

        onStatePossiblyChanged()
        return submissionResult
    }
}