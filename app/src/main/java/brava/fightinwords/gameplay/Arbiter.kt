package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.hr.EmployeeFactory
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.Factotum
import kotlinx.serialization.Serializable

class Arbiter(
    val factotum: Factotum,
    private val submissions: MutableMap<Word, SubmissionResult> = mutableMapOf(),
    private val wordScorer: WordScorer = ScrabbleScorer,
    private val language: KnownLanguage = KnownLanguage.English,
) : SubmissionJudge {
    override fun submitWord(word: Word): SubmissionResult {
        return submissions.compute(word) { word, previousSubmission ->
            when (previousSubmission) {
                is SubmissionResult.Accepted -> previousSubmission.copy(freshness = Freshness.Stale)
                is SubmissionResult.Rejected -> previousSubmission.copy(freshness = Freshness.Stale)
                null                         -> processNewSubmission(word)
            }
        }!!
    }

    private fun processNewSubmission(word: Word): SubmissionResult {
        with(factotum) {
            val found = findWord(word)

            if (found == null) {
                return SubmissionResult.Rejected(word, Freshness.Fresh)
            }

            return SubmissionResult.Accepted(
                word,
                Freshness.Fresh,
                found.source.category,
                wordScorer.getScore(word, language)
            )
        }
    }

    @Serializable
    data class SerializableState(val submissions: List<SubmissionResult>)

    companion object : EmployeeFactory<Arbiter, SerializableState> {
        override fun Arbiter.getSerializableState(): SerializableState {
            return SerializableState(submissions.values.toList())
        }

        override fun fromSerializableState(
            state: SerializableState,
            sharedResources: EmployeeFactory.SharedResources,
        ): Arbiter {
            return Arbiter(
                sharedResources.factotum,
                state.submissions.associateByTo(mutableMapOf()) { it.word }
            )
        }

        override fun SaveGameState.getEmployeeState(): SerializableState {
            TODO("Not yet implemented")
        }
    }
}