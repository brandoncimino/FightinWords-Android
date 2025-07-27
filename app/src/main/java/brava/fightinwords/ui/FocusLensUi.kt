package brava.fightinwords.ui

import androidx.compose.runtime.mutableStateOf
import brava.fightinwords.gameplay.FocusLens

class FocusLensUi<T> private constructor(
    onExpand: () -> Unit = {},
    onCollapse: () -> Unit = {},
    onFocus: (T) -> Unit = {},
    private val stateGetter: () -> FocusLens.State<T>?
) {
    private val mutableState = mutableStateOf(stateGetter())
    val state: FocusLens.State<T>?
        get() = mutableState.value

    val onExpand = { onExpand(); refresh() }
    val onCollapse = { onCollapse(); refresh() }
    val onFocus = { t: T -> onFocus(t); refresh() }

    companion object {
        fun <T> FocusLens<T>.createUi(): FocusLensUi<T> {
            return FocusLensUi<T>(
                onExpand = this::expand,
                onCollapse = this::collapse,
                onFocus = this::focusOn,
                stateGetter = { this.state }
            )
        }
    }

    fun refresh() {
        mutableState.value = stateGetter()
    }
}