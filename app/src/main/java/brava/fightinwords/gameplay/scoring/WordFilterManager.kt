package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.WordState
import brava.fightinwords.gameplay.hr.EmployeeFactory
import brava.fightinwords.gameplay.scoring.Ledgerman.Companion.getLengthFilterState

sealed interface WordFilterManager {
    fun filter(word: WordState): Boolean
    fun getFilterState(wordFilter: WordFilter): FilterState
    fun enableFilter(wordFilter: WordFilter)
    fun disableFilter(wordFilter: WordFilter)
    fun toggleFilter(wordFilter: WordFilter)
    fun clearFilters()

    fun snapshot(): SerializableState
    sealed interface SerializableState

    companion object : EmployeeFactory<WordFilterManager, SerializableState> {
        override fun WordFilterManager.getSerializableState(): SerializableState {
            return snapshot()
        }

        override fun fromSerializableState(
            state: SerializableState,
            gamePlan: GamePlan,
        ): WordFilterManager {
            return when (state) {
                is MultiSelectWordFilters.SerializableState  -> MultiSelectWordFilters(state.lengthFilters)
                is SingleSelectWordFilters.SerializableState -> SingleSelectWordFilters(state.selected)
            }
        }

        override fun SaveGameState.getEmployeeState(): SerializableState {
            return ledgermanState.wordFilters
        }

    }
}

class SingleSelectWordFilters(private var selected: WordFilter? = null) : WordFilterManager {
    override fun getFilterState(wordFilter: WordFilter): FilterState {
        return when (selected) {
            wordFilter -> FilterState.ActiveExplicitly
            null       -> FilterState.ActiveImplicitly
            else       -> FilterState.Inactive
        }
    }

    override fun filter(word: WordState) = selected?.filter(word) ?: true

    override fun enableFilter(wordFilter: WordFilter) {
        selected = wordFilter
    }

    override fun disableFilter(wordFilter: WordFilter) {
        if (selected == wordFilter) {
            selected = null
        }
    }

    override fun toggleFilter(wordFilter: WordFilter) {
        selected = when {
            selected == wordFilter -> null
            else                   -> wordFilter
        }
    }

    override fun clearFilters() {
        selected = null
    }

    @JvmInline
    value class SerializableState(val selected: WordFilter?) : WordFilterManager.SerializableState

    override fun snapshot() = SerializableState(selected)

    companion object : EmployeeFactory<SingleSelectWordFilters, SerializableState> {
        override fun SingleSelectWordFilters.getSerializableState(): SerializableState = snapshot()

        override fun fromSerializableState(
            state: SerializableState,
            gamePlan: GamePlan,
        ): SingleSelectWordFilters {
            return SingleSelectWordFilters(state.selected)
        }

        override fun SaveGameState.getEmployeeState(): SerializableState {
            TODO("Not yet implemented")
        }

    }
}

class MultiSelectWordFilters(
    private var lengthFilters: TinyFlags,
) : WordFilterManager {
    override fun getFilterState(wordFilter: WordFilter): FilterState {
        return when (wordFilter) {
            is WordFilter.LengthFilter -> lengthFilters.getLengthFilterState(wordFilter.wordLength)
        }
    }

    override fun filter(word: WordState): Boolean {
        return lengthFilters.getLengthFilterState(word.word.length).isActive
    }

    override fun enableFilter(wordFilter: WordFilter) {
        blog { "Requesting enablement of: $wordFilter" }
        return when (wordFilter) {
            is WordFilter.LengthFilter -> lengthFilters = lengthFilters.enable(wordFilter.wordLength)
        }
    }

    override fun disableFilter(wordFilter: WordFilter) {
        when (wordFilter) {
            is WordFilter.LengthFilter ->
                lengthFilters = lengthFilters.disable(wordFilter.wordLength)
        }
    }


    override fun toggleFilter(wordFilter: WordFilter) {
        blog { "Toggling the state of: $wordFilter" }
        when (wordFilter) {
            is WordFilter.LengthFilter -> {
                val currentState = lengthFilters[wordFilter.wordLength]
                val desiredState = !currentState
                val setResult = lengthFilters.set(wordFilter.wordLength, desiredState)
                lengthFilters = setResult
            }
        }
    }

    override fun clearFilters() {
        blog { "Clearing word filters: $lengthFilters" }
        lengthFilters = TinyFlags()
    }

    @JvmInline
    value class SerializableState(val lengthFilters: TinyFlags) : WordFilterManager.SerializableState

    override fun snapshot() = SerializableState(lengthFilters)
}