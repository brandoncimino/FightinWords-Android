package brava.fightinwords.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.data.Word

data class FocusedDefinitionState(
    val definedWord: DefinedWordState,
    val poppedUp: Boolean = false
)

class UmpireUi(
    val onSubmitWord: (Word) -> Umpire.SubmissionResult,
    private val visibleWordState: MutableState<List<DefinedWordState>>,
    private val focusedDefinitionState: MutableState<FocusedDefinitionState?> = mutableStateOf(null)
) {
    val focusedDefinition by focusedDefinitionState
    val visibleWords by visibleWordState

    fun submitWord(word: Word) {
        val submissionResult = onSubmitWord(word);
        if (submissionResult.wordState is DefinedWordState) {
            focusDefinition(submissionResult.wordState)
        }
    }

    fun focusDefinition(definedWordState: DefinedWordState) {
        focusedDefinitionState.value = FocusedDefinitionState(definedWordState)
    }

    fun expandDefinition() {
        focusedDefinitionState.value = focusedDefinitionState.value?.copy(poppedUp = true)
    }

    fun collapseDefinition() {
        focusedDefinitionState.value = focusedDefinitionState.value?.copy(poppedUp = false)
    }

    companion object {
        fun Umpire.createUi(): UmpireUi {
            val visibleWordState = mutableStateOf(this.visibleWords())
            val focusedDefinitionState: MutableState<FocusedDefinitionState?> = mutableStateOf(null)
            return UmpireUi(
                onSubmitWord = {
                    val result = this.submitWord(it)
                    visibleWordState.value = this.visibleWords()
                    result
                },
                focusedDefinitionState = focusedDefinitionState,
                visibleWordState = visibleWordState,
            )
        }
    }
}