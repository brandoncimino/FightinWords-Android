package brava.fightinwords.gameplay.scoring

import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.TinyFlags
import brava.fightinwords.gameplay.Arbiter
import brava.fightinwords.gameplay.Arbiter.Companion.getSerializableState
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.FocusLens
import brava.fightinwords.gameplay.SubmissionResult
import brava.fightinwords.gameplay.UnsubmittedWordVisibility
import brava.fightinwords.gameplay.WordCategory
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.WordPool
import brava.fightinwords.gameplay.hr.EmployeeFactory
import kotlinx.serialization.Serializable

class Ledgerman(
    val unsubmittedWordVisibility: UnsubmittedWordVisibility,
    val wordLengthRange: IntRange,
    val wordSorting: WordSorting = WordSorting.LengthFirst,
    focusedWord: FocusLens.State<DefinedWordState>? = null,
    val wordFilterManger: WordFilterManager = SingleSelectWordFilters(),
    val coreWordPool: WordPool,
    val arbiter: Arbiter,
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

    fun isVisible(submissionResult: SubmissionResult): Boolean {
        val word = submissionResult.word
        if (wordFilterManger.filter(word) == false) {
            return false
        }

        return when (submissionResult) {
            is SubmissionResult.Accepted -> true
            else        ->
                when (unsubmittedWordVisibility) {
                    UnsubmittedWordVisibility.None     -> false
                    UnsubmittedWordVisibility.Standard -> word is DefinedWordState && word.category == WordCategory.Core
                }
        }
    }

    @Serializable
    data class State(
        val wordFilters: WordFilterManager.SerializableState,
        val focusedWord: FocusLens.State<DefinedWordState>?,
        val umpireState: Arbiter.SerializableState,
    )

    companion object : EmployeeFactory<Ledgerman, State> {
        override fun Ledgerman.getSerializableState() = State(
            wordFilterManger.snapshot(),
            focusedWord,
            arbiter.getSerializableState()
        )

        override fun fromSerializableState(
            state: State,
            sharedResources: EmployeeFactory.SharedResources,
        ): Ledgerman {
            return Ledgerman(
                unsubmittedWordVisibility = sharedResources.gamePlan.unsubmittedWordVisibility,
                focusedWord = state.focusedWord,
                wordFilterManger = WordFilterManager.fromSerializableState(
                    state.wordFilters,
                    sharedResources
                ),
                wordSorting = sharedResources.gamePlan.scoreboardSorting,
                arbiter = Arbiter.fromSerializableState(state.umpireState, sharedResources),
                wordLengthRange = sharedResources.gamePlan.minimumWordLength..sharedResources.gamePlan.letterPool.length,
                coreWordPool = sharedResources.coreWordPool
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

    private val Word.isCore get() = coreWordPool.contains(this)

    private fun getScoreboardWordVisibility(word: Word): ScoreboardWord? {
        val state = arbiter.getCurrentStateOf(word)

        return when (state) {
            is SubmissionResult.Accepted -> ScoreboardWordVisibility.Full
            is SubmissionResult.Rejected -> null
            null                         -> when (unsubmittedWordVisibility) {
                UnsubmittedWordVisibility.None -> null
                UnsubmittedWordVisibility.Standard -> when {
                    word.isCore -> ScoreboardWordVisibility.Masked
                    else        -> null
                }
            }
        }?.let {
            ScoreboardWord(word, it)
        }
    }

    /**
     * TODO: This is a very important method to optimize - however, it needs to be optimized SPECIFICALLY with the intent to reduce recompositions.
     */
    fun getScoreboardWords(): List<ScoreboardWord> {
        return sequence {
            yieldAll(arbiter.acceptedWords())
            yieldAll(coreWordPool.words)
        }
            .distinct()
            .mapNotNull { getScoreboardWordVisibility(it) }
            .toList()
    }

    fun expandFocusedWord() = focusedWordLens.expand()
    fun collapseFocusedWord() = focusedWordLens.collapse()
    fun focusOnWord(wordState: DefinedWordState) = focusedWordLens.focusOn(wordState)

    enum class WordSorting(val comparator: Comparator<Word>) : Comparator<Word> by comparator {
        /**
         * AKA "[shortlex order](https://en.wikipedia.org/wiki/Shortlex_order)".
         */
        LengthFirst(Word::shortlexCompare),
        Lexicographical(Word::lexicographicalCompare),
    }
}

data class ScoreboardWord(
    val word: Word,
    val visibility: ScoreboardWordVisibility,
)

enum class ScoreboardWordVisibility {
    Masked,
    Full
}