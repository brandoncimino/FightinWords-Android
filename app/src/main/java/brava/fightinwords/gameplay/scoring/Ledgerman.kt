package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.gameplay.*
import brava.fightinwords.gameplay.Umpire.Companion.getSerializableState
import brava.fightinwords.gameplay.hr.EmployeeFactory
import kotlinx.serialization.Serializable

class Ledgerman(
    val unsubmittedWordVisibility: UnsubmittedWordVisibility,
    val wordLengthRange: IntRange,
    val wordSorting: WordSorting = WordSorting.LengthFirst,
    focusedWord: FocusLens.State<DefinedWordState>? = null,
    val wordFilterManger: WordFilterManager = SingleSelectWordFilters(),
    val umpire: Umpire,
) {
    internal val focusedWordLens = FocusLens(focusedWord)
    val focusedWord by focusedWordLens

    fun toggleWordFilter(wordFilter: WordFilter) {
        wordFilterManger.toggleFilter(wordFilter)
    }

    fun wordFilterSnapshot(): List<WordFilter.State> {
        return wordLengthFiltersSnapshot()
    }

    private fun wordLengthFiltersSnapshot(): List<WordFilter.State> {
        return wordLengthRange.map {
            val lengthFilter = WordFilter.LengthFilter(it)
            WordFilter.State(
                lengthFilter,
                wordFilterManger.getFilterState(lengthFilter)
            )
        }
    }

    fun isVisible(wordState: WordState): Boolean {
        if (wordFilterManger.filter(wordState) == false) {
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
        val wordFilters: WordFilterManager.SerializableState,
        val focusedWord: FocusLens.State<DefinedWordState>?,
        val umpireState: Umpire.SerializableState,
    )

    companion object : EmployeeFactory<Ledgerman, State> {
        override fun Ledgerman.getSerializableState() = State(
            wordFilterManger.snapshot(),
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
                wordFilterManger = WordFilterManager.fromSerializableState(state.wordFilters, gamePlan),
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
}