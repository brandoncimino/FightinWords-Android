package brava.fightinwords.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.UnsubmittedWordVisibility
import brava.fightinwords.gameplay.WordState

class UmpireUi(
    private val stateGetter: () -> List<WordState>,
) {
    private val visibleWordState = mutableStateOf(stateGetter())
    val visibleWords by visibleWordState

    companion object {
        fun Umpire.createUi(unsubmittedWordVisibility: UnsubmittedWordVisibility): UmpireUi {
            return UmpireUi(
                stateGetter = { this.visibleWords(unsubmittedWordVisibility) }
            )
        }
    }

    fun refresh() {
        this.visibleWordState.value = stateGetter()
    }
}