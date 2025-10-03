package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.scoring.WordScoringStrategy

/**
 * Something capable of producing usable objects based on serializable IDs like [WordList.Id].
 */
interface WordSourceLoader {
    fun getDefinitionLookup(id: DefinitionLookup.Id): DefinitionLookup
    fun getWordLookup(id: WordLookup.Id): WordLookup
    fun getWordList(id: WordList.Id): WordList
    fun getWordScorer(strategy: WordScoringStrategy): WordScorer

    val allDefinitionLookups: List<DefinitionLookup>
}