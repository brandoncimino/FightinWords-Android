package brava.fightinwords.gameplay.scoring

import brava.fightinwords.gameplay.WordState
import kotlinx.serialization.Serializable

@Serializable
sealed interface WordFilter {
    fun filter(wordState: WordState): Boolean

    @Serializable
    @JvmInline
    value class LengthFilter(val wordLength: Int) : WordFilter {
        override fun filter(wordState: WordState): Boolean {
            return wordState.word.length == wordLength
        }

        override val label get() = wordLength.toString()
    }

    @Serializable
    data class State(
        val wordFilter: WordFilter,
        val filterState: FilterState,
    )

    val label: String
}


//data class WordLengthFilterState(val wordLength: Int, override val filterState: FilterState) : WordFilter.State

enum class FilterState {
    Inactive,
    ActiveImplicitly,
    ActiveExplicitly,
    ;

    val isActive
        get() = when (this) {
            Inactive -> false
            else     -> true
        }
}