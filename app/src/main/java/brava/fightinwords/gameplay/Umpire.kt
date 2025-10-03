package brava.fightinwords.gameplay

import android.util.Log
import brava.fightinwords.SaveGameState
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.hr.EmployeeFactory
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import kotlinx.serialization.Serializable

/**
 * Decides what is and isn't legal.
 */
@Deprecated("The time of the Arbiter is now.")
class Umpire private constructor(
    private val wordStates: MutableMap<Word, WordStateFlavor>,
    private val wordScorer: WordScorer = ScrabbleScorer,
    private val language: KnownLanguage = KnownLanguage.English,
) : SubmissionJudge {
    constructor(
        wordPool: Sequence<Word>,
        wordScorer: WordScorer,
        language: KnownLanguage = KnownLanguage.English,
    ) : this(
        wordStates = wordPool.associateWithTo(mutableMapOf()) {
            WordStateFlavor.Unplayed
        },
        wordScorer = wordScorer,
        language = language
    )

    companion object : EmployeeFactory<Umpire, SerializableState> {
        override fun Umpire.getSerializableState(): SerializableState {
            val flavorMap = buildMap {
                wordStates.forEach { (word, flavor) ->
                    val flavorGroup = getOrPut(flavor, { mutableListOf<Word>() })
                    flavorGroup.add(word)
                }
            }

            return SerializableState(flavorMap)
        }

        override fun fromSerializableState(
            state: SerializableState,
            sharedResources: EmployeeFactory.SharedResources,
        ): Umpire {
            return Umpire(
                state.flavorMap.reverseTo(mutableMapOf())
            )
        }

        override fun SaveGameState.getEmployeeState(): SerializableState =
            TODO()//ledgermanState.umpireState
    }

    init {
        blog { "Created ${this.javaClass.simpleName} with a pool of ${wordStates.size} playable words" }
    }

    private fun getScore(word: Word): Int = wordScorer.getScore(word, language)

    override fun submitWord(word: Word): SubmissionResult {
        blog(Log.DEBUG) { "Submitting word $word" }
        val previousState = wordStates[word]

        val result = when (previousState) {
            WordStateFlavor.Accepted -> SubmissionResult.Accepted(
                word,
                Freshness.Stale,
                WordCategory.Core/*TODO*/,
                getScore(word)
            )

            WordStateFlavor.Unplayed -> acceptFreshWord(word)
            WordStateFlavor.Rejected -> SubmissionResult.Rejected(word, Freshness.Stale)
            null -> rejectFreshWord(word)
        }

        blog(Log.DEBUG) { "Ruled the submission: $result" }
        return result
    }

    private fun acceptFreshWord(unplayedWord: Word): brava.fightinwords.gameplay.SubmissionResult {
        check(wordStates[unplayedWord] == WordStateFlavor.Unplayed) {
            "The word `$unplayedWord` doesn't have the state ${WordStateFlavor.Unplayed}"
        }

        wordStates[unplayedWord] = WordStateFlavor.Accepted
        return SubmissionResult.Accepted(
            unplayedWord,
            Freshness.Fresh,
            WordCategory.Core,
            getScore(unplayedWord)
        )
    }

    private fun rejectFreshWord(word: Word): brava.fightinwords.gameplay.SubmissionResult {
        check(wordStates.contains(word) == false) { "The word `$word` can't be rejected fresh because it's already been submitted." }

        wordStates[word] = WordStateFlavor.Rejected
        return SubmissionResult.Rejected(word, Freshness.Fresh)
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
        val flavorMap: Map<WordStateFlavor, List<Word>>,
    )
}

private fun <K, V, M : MutableMap<V, K>> Map<K, Iterable<V>>.reverseTo(destination: M): M {
    forEach { (flavor, words) ->
        words.forEach {
            destination.put(it, flavor)
        }
    }

    return destination
}