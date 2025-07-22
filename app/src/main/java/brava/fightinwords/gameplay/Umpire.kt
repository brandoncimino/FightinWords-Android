package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.WordDefinition

/**
 * Decides what is and isn't legal.
 */
class Umpire(
    wordPool: Sequence<WordDefinition>,
    val wordScorer: WordScorer,
    val language: KnownLanguage = KnownLanguage.English,
    val unsubmittedWordVisibility: UnsubmittedWordVisibility = UnsubmittedWordVisibility.Standard
) {
    private val wordStates: MutableMap<Word, WordState> = wordPool
        .sortedBy { it.word.length }
        .associate {
        it.word to Unplayed(it)
    }
        .toMutableMap()

    init {
        println("Created ${this.javaClass.simpleName} with a pool of ${wordStates.size} playable words (${wordStates.count { (it.value as DefinedWordState).wordDefinition.isNaspaWord }} NASPA standard)")
    }

    fun submitWord(word: Word): SubmissionResult {
        println("Submitting the word: $word")
        val previousState = wordStates[word]

        val result = when (previousState) {
            is Accepted -> SubmissionResult(Freshness.Stale, previousState)
            is Unplayed -> acceptFreshWord(previousState)
            is Rejected -> SubmissionResult(Freshness.Stale, previousState)
            null -> rejectFreshWord(word)
        }

        println("Ruled the submission: $result")
        return result
    }

    fun getCurrentState(word: Word): WordState? = wordStates[word]

    private fun acceptFreshWord(unplayedWord: Unplayed): SubmissionResult {
        check(wordStates[unplayedWord.word] == unplayedWord)
        val accepted = Accepted(unplayedWord.wordDefinition, wordScorer.getScore(unplayedWord.word, language))
        wordStates[unplayedWord.word] = accepted
        return SubmissionResult(Freshness.Fresh, accepted)
    }

    private fun rejectFreshWord(word: Word): SubmissionResult {
        check(wordStates.contains(word) == false)
        val rejected = Rejected(word)
        wordStates[word] = rejected
        return SubmissionResult(Freshness.Fresh, rejected)
    }

    data class SubmissionResult(
        val freshness: Freshness,
        val wordState: WordState,
    )

    fun snapshot(): List<WordState> {
        return wordStates.values.toList()
    }

    private fun DefinedWordState.isVisible(): Boolean {
        return when (this) {
            is Accepted -> true
            else ->
                when (unsubmittedWordVisibility) {
                    UnsubmittedWordVisibility.None -> false
                    UnsubmittedWordVisibility.Standard -> this.wordDefinition.isNaspaWord
                    UnsubmittedWordVisibility.All -> true
                }
        }
    }

    fun visibleWords(): List<DefinedWordState> {
        val visibles = wordStates.values
            .filterIsInstance<DefinedWordState>()
            .filter { it.isVisible() }

        println("Returning ${visibles.size} visible words")
        return visibles
    }
}

enum class UnsubmittedWordVisibility {
    None,
    Standard,
    All
}

enum class Freshness {
    Fresh,
    Stale
}

sealed interface WordState {
    val word: Word
}

sealed interface DefinedWordState : WordState {
    val wordDefinition: WordDefinition
    override val word get() = wordDefinition.word
}

data class Unplayed(override val wordDefinition: WordDefinition) : DefinedWordState
data class Rejected(override val word: Word) : WordState
data class Accepted(override val wordDefinition: WordDefinition, val points: Int) : DefinedWordState
