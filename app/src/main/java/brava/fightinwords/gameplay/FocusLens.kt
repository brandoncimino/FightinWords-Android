package brava.fightinwords.gameplay

import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty

class FocusLens<T>(initialState: State<T>? = null) {
    constructor(initialValue: T?, zoomed: Boolean = false) : this(initialValue?.let { State(it, zoomed) })

    var state: State<T>? = initialState
        private set

    fun focusOn(target: T, zoomed: Boolean = false) {
        state = State(target, zoomed)
    }

    private fun setZoom(zoomed: Boolean) = state?.let { state = it.copy(zoomed = zoomed) }
    fun expand() = setZoom(true)
    fun collapse() = setZoom(false)

    fun setFocus(state: State<T>?) {
        this.state = state
    }

    @Serializable
    data class State<T>(
        val target: T,
        val zoomed: Boolean = false
    ) {
        inline fun <R> map(mapper: (T) -> R): State<R> {
            return State(mapper(target), zoomed)
        }
    }

    /**
     * Funky syntax that lets you use that fancy `val x by y` syntax.
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): State<T>? = state
}