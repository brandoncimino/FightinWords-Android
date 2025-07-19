package brava.fightinwords.ui.typesetter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import brava.fightinwords.gameplay.Typesetter

data class TypesetterUi(
    private val mutableState: MutableState<TypesetterState>,
    val buttons: TypesetterButtons
) {
    val state by mutableState

    companion object {
        fun Typesetter.createUi(): TypesetterUi {
            val mutableState = mutableStateOf(this.snapshot())
            val buttons = this.buttons().then { mutableState.value = snapshot() }

            return TypesetterUi(
                mutableState,
                buttons
            )
        }
    }
}