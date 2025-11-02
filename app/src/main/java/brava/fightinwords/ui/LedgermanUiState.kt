package brava.fightinwords.ui

import brava.fightinwords.gameplay.FocusLens
import brava.fightinwords.gameplay.scoring.FocusedWord
import brava.fightinwords.gameplay.scoring.Ledgerman
import brava.fightinwords.gameplay.scoring.ScoreboardWord
import brava.fightinwords.gameplay.scoring.WordFilter

data class LedgermanUiState(
    val wordFilterStates: List<WordFilter.State>,
    val visibleWords: List<ScoreboardWord>,
    val focusedWord: FocusLens.State<FocusedWord>?,
) {
    companion object {
        fun Ledgerman.getUiState(): LedgermanUiState {
            return LedgermanUiState(
                wordFilterStates = wordFilterSnapshot(),
                visibleWords = getScoreboardWords(),
                focusedWord = focusedWord
            )
        }
    }
}