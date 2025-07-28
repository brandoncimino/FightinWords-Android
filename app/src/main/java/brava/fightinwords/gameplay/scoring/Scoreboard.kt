package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.gameplay.*
import kotlinx.serialization.Serializable

class Scoreboard(
    val unsubmittedWordVisibility: UnsubmittedWordVisibility,
    val wordSorting: WordSorting = WordSorting.LengthFirst,
    focusedWord: FocusLens.State<DefinedWordState>? = null,
    private var enabledLengthFilters: TinyFlags = TinyFlags(),
) {
    private val focusedWordLens = FocusLens(focusedWord)
    val focusedWord by focusedWordLens

    fun setWordLengthFilter(wordLength: Int, enabled: Boolean): TinyFlags {
        return enabledLengthFilters.set(wordLength, enabled)
    }

    fun enableWordLengthFilter(wordLength: Int) = enabledLengthFilters.enable(wordLength)
    fun disableWordLengthFilter(wordLength: Int) = enabledLengthFilters.disable(wordLength)

    fun toggleWordLengthFilter(wordLength: Int) {
        setWordLengthFilter(wordLength, enabledLengthFilters[wordLength].not())
    }

    enum class FilterState {
        Disabled,
        EnabledImplicitly,
        EnabledExplicitly,
        ;

        val isEnabled
            get() = when (this) {
                Disabled -> false
                else     -> true
            }
    }

    fun getLengthFilterState(wordLength: Int): FilterState {
        return when {
            enabledLengthFilters.isEmpty()            -> FilterState.EnabledImplicitly
            enabledLengthFilters.contains(wordLength) -> FilterState.EnabledExplicitly
            else                                      -> FilterState.Disabled
        }
    }

    fun isLengthVisible(wordLength: Int): Boolean {
        return getLengthFilterState(wordLength).isEnabled
    }

    fun clearLengthFilters() {
        enabledLengthFilters = TinyFlags()
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
    data class SerializableState(
        val enabledLengthFilters: TinyFlags,
        val focusedWord: FocusLens.State<DefinedWordState>?
    )

    companion object : EmployeeFactory<Scoreboard, SerializableState> {
        override fun Scoreboard.getSerializableState() = SerializableState(
            enabledLengthFilters,
            focusedWord
        )

        override fun fromSerializableState(
            state: SerializableState,
            gamePlan: GamePlan
        ): Scoreboard {
            return Scoreboard(
                unsubmittedWordVisibility = gamePlan.unsubmittedWordVisibility,
                focusedWord = state.focusedWord,
                enabledLengthFilters = state.enabledLengthFilters,
                wordSorting = gamePlan.scoreboardSorting
            )
        }

        override fun SaveGameState.getEmployeeState(): SerializableState = scoreboardState
    }

    fun getVisibleWords(wordStates: Iterable<WordState>): List<WordState> {
        return wordStates.filter { isVisible(it) }
            .sortedWith(wordSorting)
    }

    fun focusOnWord(wordState: DefinedWordState) {
        this.focusedWordLens.focusOn(wordState)
    }

    enum class WordSorting(val comparator: Comparator<WordState>) : Comparator<WordState> by comparator {
        LengthFirst(Comparator.comparing<WordState, Int> { it.word.length }.thenBy { it.word }),
        Alphabetical(Comparator.comparing { it.word }),
    }
}