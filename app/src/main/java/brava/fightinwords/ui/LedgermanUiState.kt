package brava.fightinwords.ui

import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.FocusLens
import brava.fightinwords.gameplay.WordState
import brava.fightinwords.gameplay.scoring.Ledgerman
import brava.fightinwords.gameplay.scoring.WordFilter

data class LedgermanUiState(
    val wordFilterStates: List<WordFilter.State>,
    val visibleWords: List<WordState>,
    val focusedWord: FocusLens.State<DefinedWordState>?,
) {
    companion object {
        fun Ledgerman.getUiState(): LedgermanUiState {
            return LedgermanUiState(
                wordFilterStates = wordFilterSnapshot(),
                visibleWords = getVisibleWords(),
                focusedWord = focusedWord
            )
        }
    }
}