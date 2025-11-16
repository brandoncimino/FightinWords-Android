package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.hr.EmployeeFactory
import brava.fightinwords.gameplay.scoring.Ledgerman.Companion.getLengthFilterState
import kotlinx.serialization.Serializable

sealed interface WordFilterManager {
    fun filter(word: Word): Boolean
    fun getFilterState(wordFilter: WordFilter): FilterState
    fun enableFilter(wordFilter: WordFilter)
    fun disableFilter(wordFilter: WordFilter)
    fun toggleFilter(wordFilter: WordFilter)
    fun clearFilters()

    fun snapshot(): SerializableState

    @Serializable
    sealed interface SerializableState

    companion object : EmployeeFactory<WordFilterManager, SerializableState> {
        override fun WordFilterManager.getSerializableState(): SerializableState {
            return snapshot()
        }

        override fun fromSerializableState(
            state: SerializableState,
            sharedResources: EmployeeFactory.SharedResources,
        ): WordFilterManager {
            return when (state) {
                is MultiSelectWordFilters.MultiSelectSerializableState   -> MultiSelectWordFilters(state.lengthFilters)
                is SingleSelectWordFilters.SingleSelectSerializableState -> SingleSelectWordFilters(state.selected)
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

    override fun filter(word: Word) = selected?.filter(word) ?: true

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

    @Serializable
    data class SingleSelectSerializableState(val selected: WordFilter?) :
        WordFilterManager.SerializableState

    override fun snapshot() = SingleSelectSerializableState(selected)

    companion object : EmployeeFactory<SingleSelectWordFilters, SingleSelectSerializableState> {
        override fun SingleSelectWordFilters.getSerializableState(): SingleSelectSerializableState = snapshot()

        override fun fromSerializableState(
            state: SingleSelectSerializableState,
            sharedResources: EmployeeFactory.SharedResources,
        ): SingleSelectWordFilters {
            return SingleSelectWordFilters(state.selected)
        }

        override fun SaveGameState.getEmployeeState(): SingleSelectSerializableState {
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

    override fun filter(word: Word): Boolean {
        return lengthFilters.getLengthFilterState(word.length).isActive
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

    @Serializable
    data class MultiSelectSerializableState(val lengthFilters: TinyFlags) :
        WordFilterManager.SerializableState

    override fun snapshot() = MultiSelectSerializableState(lengthFilters)
}