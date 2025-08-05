package brava.fightinwords.ui

import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.ui.LedgermanUiState.Companion.getUiState
import brava.fightinwords.ui.typesetter.TypesetterState
import brava.fightinwords.ui.typesetter.snapshot

sealed interface GameScreenState {
    object Loading : GameScreenState

    data class InGame(
        val typesetterState: TypesetterState,
        val ledgermanState: LedgermanUiState,
    ) : GameScreenState {
        companion object {
            fun GameInProgress.getScreenState(): InGame {
                return InGame(
                    typesetterState = typesetter.snapshot(),
                    ledgermanState = ledgerman.getUiState(),
                )
            }
        }
    }
}