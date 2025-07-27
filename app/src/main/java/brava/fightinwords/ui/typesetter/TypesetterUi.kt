package brava.fightinwords.ui.typesetter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import brava.fightinwords.gameplay.Typesetter

class TypesetterUi(
    private val stateGetter: () -> TypesetterState,
    buttons: TypesetterButtons
) {
    private val mutableState = mutableStateOf(stateGetter())
    val state by mutableState
    val buttons = buttons.then { refresh() }

    companion object {
        fun Typesetter.createUi(
            onSubmitWord: () -> Unit,
            stateGetter: () -> TypesetterState = { snapshot() }
        ): TypesetterUi {
            val buttons = this.buttons(onSubmitWord)

            return TypesetterUi(
                stateGetter,
                buttons
            )
        }
    }

    fun refresh() {
        mutableState.value = stateGetter()
    }
}