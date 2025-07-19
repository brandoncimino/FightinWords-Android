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
    val language: KnownLanguage = KnownLanguage.English
) {
    private val playableWords: MutableMap<Word, WordState> = wordPool.associate {
        it.word to Unplayed(it)
    }
        .toMutableMap()

    init {
        println("Created ${this.javaClass.simpleName} with a pool of ${playableWords.size} playable words")
    }

    fun submitWord(word: Word): SubmissionResult {
        val previousState = playableWords[word]

        return when (previousState) {
            is Accepted -> SubmissionResult(Freshness.Stale, previousState)
            is Unplayed -> acceptFreshWord(previousState)
            is Rejected -> SubmissionResult(Freshness.Stale, previousState)
            null -> rejectFreshWord(word)
        }
    }

    fun getCurrentState(word: Word): WordState? = playableWords[word]

    private fun acceptFreshWord(unplayedWord: Unplayed): SubmissionResult {
        check(playableWords[unplayedWord.word] == unplayedWord)
        val accepted = Accepted(unplayedWord.wordDefinition, wordScorer.getScore(unplayedWord.word, language))
        playableWords[unplayedWord.word] = accepted
        return SubmissionResult(Freshness.Fresh, accepted)
    }

    private fun rejectFreshWord(word: Word): SubmissionResult {
        check(playableWords.contains(word) == false)
        val rejected = Rejected(word)
        playableWords[word] = rejected
        return SubmissionResult(Freshness.Fresh, rejected)
    }

    data class SubmissionResult(
        val freshness: Freshness,
        val wordState: WordState,
    )

    fun snapshot(): List<WordState> {
        return playableWords.values.toList()
    }
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
