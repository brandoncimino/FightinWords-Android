package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.BlogTimeline.Companion.blogged
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.*
import brava.fightinwords.gameplay.Umpire.Companion.getSerializableState
import brava.fightinwords.gameplay.hr.EmployeeFactory
import kotlinx.serialization.Serializable

class Ledgerman(
    val unsubmittedWordVisibility: UnsubmittedWordVisibility,
    val wordLengthRange: IntRange,
    val wordSorting: WordSorting = WordSorting.LengthFirst,
    focusedWord: FocusLens.State<DefinedWordState>? = null,
    lengthFilters: TinyFlags = TinyFlags(),
    val umpire: Umpire,
) {
    internal val focusedWordLens = FocusLens(focusedWord)
    val focusedWord by focusedWordLens

    var lengthFilters by blogged(lengthFilters)

    fun setWordFilter(wordFilter: WordFilter, enabled: Boolean) {
        when (wordFilter) {
            is WordFilter.LengthFilter -> lengthFilters = lengthFilters.set(wordFilter.wordLength, enabled)
        }
    }

    fun enableWordFilter(wordFilter: WordFilter): TinyFlags {
        blog { "Requesting enablement of: $wordFilter" }
        return when (wordFilter) {
            is WordFilter.LengthFilter -> lengthFilters.enable(wordFilter.wordLength)
        }
    }

    fun disableWordFilter(wordFilter: WordFilter) {
        when (wordFilter) {
            is WordFilter.LengthFilter ->
                lengthFilters = lengthFilters.disable(wordFilter.wordLength)
        }
    }

    fun toggleWordFilter(wordFilter: WordFilter) {
        blog { "Toggling the state of: $wordFilter" }
        when (wordFilter) {
            is WordFilter.LengthFilter -> {
                val currentState = lengthFilters[wordFilter.wordLength]
                val desiredState = !currentState
                blog { "Current state: $currentState; desired: $desiredState" }
                blog { "Before `set`: $lengthFilters" }
                val setResult = lengthFilters.set(wordFilter.wordLength, !lengthFilters.contains(wordFilter.wordLength))
                blog { "`set` result: $setResult" }
                lengthFilters = setResult
            }
        }
    }

    fun isLengthVisible(wordLength: Int): Boolean {
        return lengthFilters.getLengthFilterState(wordLength).isActive
    }

    fun clearWordFilters() {
        lengthFilters = TinyFlags()
    }

    fun wordFilterSnapshot(): List<WordFilter.State> {
        return wordLengthFiltersSnapshot()
    }

    private fun wordLengthFiltersSnapshot(): List<WordFilter.State> {
        return wordLengthRange.map {
            WordFilter.State(
                WordFilter.LengthFilter(it),
                lengthFilters.getLengthFilterState(it)
            )
        }
    }

    fun isVisible(wordState: WordState): Boolean {
        if (isLengthVisible(wordState.word.length) == false) {
            return false
        }

        return when (wordState) {
            is Accepted -> true
            else        ->
                when (unsubmittedWordVisibility) {
                    UnsubmittedWordVisibility.None     -> false
                    UnsubmittedWordVisibility.Standard -> wordState is DefinedWordState && wordState.wordDefinition.isNaspaWord
                    UnsubmittedWordVisibility.All      -> true
                }
        }
    }

    @Serializable
    data class State(
        val enabledLengthFilters: TinyFlags,
        val focusedWord: FocusLens.State<DefinedWordState>?,
        val umpireState: Umpire.SerializableState,
    )

    companion object : EmployeeFactory<Ledgerman, State> {
        override fun Ledgerman.getSerializableState() = State(
            lengthFilters,
            focusedWord,
            umpire.getSerializableState()
        )

        override fun fromSerializableState(
            state: State,
            gamePlan: GamePlan,
        ): Ledgerman {
            return Ledgerman(
                unsubmittedWordVisibility = gamePlan.unsubmittedWordVisibility,
                focusedWord = state.focusedWord,
                lengthFilters = state.enabledLengthFilters,
                wordSorting = gamePlan.scoreboardSorting,
                umpire = Umpire.fromSerializableState(state.umpireState, gamePlan),
                wordLengthRange = gamePlan.minimumWordLength..gamePlan.letterPool.size
            )
        }

        override fun SaveGameState.getEmployeeState(): State = ledgermanState

        fun TinyFlags.getLengthFilterState(wordLength: Int): FilterState {
            return when {
                isEmpty()            -> FilterState.ActiveImplicitly
                contains(wordLength) -> FilterState.ActiveExplicitly
                else                 -> FilterState.Inactive
            }
        }
    }

    fun getVisibleWords(): List<WordState> {
        return umpire.getSerializableState()
            .wordStates
            .filter { isVisible(it) }
            .sortedWith(wordSorting)
    }

    fun expandFocusedWord() = focusedWordLens.expand()
    fun collapseFocusedWord() = focusedWordLens.collapse()
    fun focusOnWord(wordState: DefinedWordState) = focusedWordLens.focusOn(wordState)

    enum class WordSorting(val comparator: Comparator<WordState>) : Comparator<WordState> by comparator {
        LengthFirst(Comparator.comparing<WordState, Int> { it.word.length }.thenBy { it.word }),
        Alphabetical(Comparator.comparing { it.word }),
    }

    enum class FilterSelectStyle {
        MultiSelect,
        SingleSelect
    }

    fun selectFilter(
        wordFilter: WordFilter.State,
        filterSelectStyle: FilterSelectStyle,
    ) {
        when (filterSelectStyle) {
            FilterSelectStyle.MultiSelect  -> toggleWordFilter(wordFilter.wordFilter)
            FilterSelectStyle.SingleSelect -> when {
                wordFilter.filterState.isActive -> clearWordFilters()
                else                            -> {
                    clearWordFilters()
                    enableWordFilter(wordFilter.wordFilter)
                }
            }
        }
    }
}