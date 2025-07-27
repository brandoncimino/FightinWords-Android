package brava.fightinwords.gameplay

import android.util.Log
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import kotlinx.serialization.Serializable

/**
 * Decides what is and isn't legal.
 */
class Umpire private constructor(
    private val wordStates: MutableMap<Word, WordState>,
    private val wordScorer: WordScorer = ScrabbleScorer,
    private val language: KnownLanguage = KnownLanguage.English
) {
    constructor(
        wordPool: Sequence<WordDefinition>,
        wordScorer: WordScorer,
        language: KnownLanguage = KnownLanguage.English
    ) : this(
        wordStates = wordPool.toWordStatesMap(),
        wordScorer = wordScorer,
        language = language
    )

    constructor(
        wordStates: Iterable<WordState>,
        wordScorer: WordScorer = ScrabbleScorer,
        language: KnownLanguage = KnownLanguage.English
    ) : this(
        wordStates = wordStates.toWordStatesMap(),
        wordScorer = wordScorer,
        language = language
    )

    companion object {
        private fun Iterable<WordState>.toWordStatesMap(): MutableMap<Word, WordState> {
            return this
                .sortedBy { it.word.length /*TODO: sorting should be stricter and depend on the current `GamePlan`, i.e. probably not be the responsibility of the `Umpire`*/ }
                .associateByTo(mutableMapOf()) { it.word }
        }


        private fun Sequence<WordDefinition>.toWordStatesMap(): MutableMap<Word, WordState> {
            return this
                .sortedBy { it.word.length /*TODO: sorting should be stricter and depend on the current `GamePlan`, i.e. probably not be the responsibility of the `Umpire`*/ }
                .associateTo(mutableMapOf()) {
                    it.word to Unplayed(it)
                }
        }
    }

    init {
        blog { "Created ${this.javaClass.simpleName} with a pool of ${wordStates.size} playable words (${wordStates.count { it is DefinedWordState && it.wordDefinition.isNaspaWord }} NASPA standard)" }
    }

    fun submitWord(word: Word): SubmissionResult {
        blog(Log.DEBUG) { "Submitting word $word" }
        val previousState = wordStates[word]

        val result = when (previousState) {
            is Accepted -> SubmissionResult(Freshness.Stale, previousState)
            is Unplayed -> acceptFreshWord(previousState)
            is Rejected -> SubmissionResult(Freshness.Stale, previousState)
            null -> rejectFreshWord(word)
        }

        blog(Log.DEBUG) { "Ruled the submission: $result" }
        return result
    }

    fun getCurrentState(word: Word): WordState? = wordStates[word]

    private fun acceptFreshWord(unplayedWord: Unplayed): SubmissionResult {
        check(wordStates[unplayedWord.word] == unplayedWord)
        val accepted = Accepted(unplayedWord.wordDefinition, wordScorer.getScore(unplayedWord.word, language))
        wordStates[unplayedWord.word] = accepted
//        refreshState()
        return SubmissionResult(Freshness.Fresh, accepted)
    }

    private fun rejectFreshWord(word: Word): SubmissionResult {
        check(wordStates.contains(word) == false)
        val rejected = Rejected(word)
        wordStates[word] = rejected
//        refreshState()
        return SubmissionResult(Freshness.Fresh, rejected)
    }

    data class SubmissionResult(
        val freshness: Freshness,
        val wordState: WordState,
    )

    fun snapshot(): List<WordState> {
        return wordStates.values.toList()
    }

    private fun DefinedWordState.isVisible(visibility: UnsubmittedWordVisibility): Boolean {
        return when (this) {
            is Accepted -> true
            else ->
                when (visibility) {
                    UnsubmittedWordVisibility.None -> false
                    UnsubmittedWordVisibility.Standard -> this.wordDefinition.isNaspaWord
                    UnsubmittedWordVisibility.All -> true
                }
        }
    }

    fun visibleWords(unsubmittedWordVisibility: UnsubmittedWordVisibility): List<DefinedWordState> {
        val visibles = wordStates.values
            .filterIsInstance<DefinedWordState>()
            .filter { it.isVisible(unsubmittedWordVisibility) }

        blog(Log.DEBUG) { "Returning ${visibles.size} visible words" }
        return visibles
    }

    @Serializable
    @JvmInline
    value class SerializableState(
        // TODO: What do we do with the DEFINITIONS of the `wordStates`?
        //   🅰️ Save them to the instance state
        //     + Easiest to implement
        //     ~ Limiting factor(s): serializing / deserializing; size of instance state
        //     + Works well when handling all of the `wordStates` as a single entity
        //   🅱️ Discard them; re-process `definitions.csv` on re-opening
        //     + Least pressure on the instance state
        //     - More complicated to implement
        //     - Limiting factor(s): re-reading and filtering the whole definitions file...!
        //   ©️ Medium-term storage (like a cache file or something?)
        //   🫠 Lazy-load definitions only when requested by the UI
        //     + Has the potential to do the least work
        //     - If it isn't implemented well, it will do the same work as option 🅱️ - possibly less efficiently!
        //     + Fastest when switching to/from the app
        //     - Slowest when submitting each word
        //       ~ A "hybrid" version, that asynchronously **starts** option 🅱️, then waits for specifically requested definitions, could speed this up
        val wordStates: List<WordState>
    )
}

enum class UnsubmittedWordVisibility {
    None,
    Standard,
    All;
}

enum class Freshness {
    Fresh,
    Stale
}

