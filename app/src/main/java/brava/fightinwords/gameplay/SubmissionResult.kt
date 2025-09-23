package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word

/**
 * This is basically [WordState], but without the [DefinedWordState.wordDefinition].
 */
sealed interface SubmissionResult {
    val word: Word
    val freshness: Freshness

    data class Accepted(
        override val word: Word,
        override val freshness: Freshness,
        val points: Int,
    ) : SubmissionResult

    data class Rejected(
        override val word: Word,
        override val freshness: Freshness,
    ) : SubmissionResult
}