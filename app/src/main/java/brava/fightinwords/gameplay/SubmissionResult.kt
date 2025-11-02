package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

@Serializable
sealed interface SubmissionResult {
    val word: Word

    // TODO: We really don't need to serialize the Freshness
    val freshness: Freshness

    @Serializable
    data class Accepted(
        override val word: Word,
        override val freshness: Freshness,
        val category: WordCategory,
        val points: Int,
    ) : SubmissionResult

    @Serializable
    data class Rejected(
        override val word: Word,
        override val freshness: Freshness,
    ) : SubmissionResult
}