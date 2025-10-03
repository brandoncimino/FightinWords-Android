package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.requireNoDuplicates
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.WordCategory
import brava.fightinwords.gameplay.data.Word
import kotlin.random.Random

/**
 * Looks up [brava.fightinwords.gameplay.data.Word]s in different sources.
 */
class Factotum(
    private val coreWordList: WordList,
    private val bonusWordLookups: List<WordLookup>,
    private val definitionLookups: List<DefinitionLookup>,
) : DefinitionLookup {
    init {
        check(definitionLookups.size > 0) { "You must provide at least 1 ${DefinitionLookup::class.simpleName}!" }
        definitionLookups.requireNoDuplicates()
    }

    private fun WordLookup.findWord(word: Word): FoundWord? {
        if (isWord(word)) {
            return FoundWord(word, id, id.category)
        }

        return null
    }

    fun findWord(word: Word): FoundWord? {
        return coreWordList.findWord(word) ?: bonusWordLookups.firstNotNullOf { it.findWord(word) }
    }

    override fun findDefinition(word: Word): WordDefinition? {
        return definitionLookups
            .firstNotNullOf { it.findDefinition(word) }
    }

    override fun findAllDefinitions(word: Word): Sequence<WordDefinition> {
        return definitionLookups
            .asSequence()
            .flatMap { it.findAllDefinitions(word) }
    }

    fun getRandomCoreWord(desiredLength: Int, random: Random = Random): Word {
        return coreWordList.getRandomWord(desiredLength, random)
    }

    val WordLookup.Id.category: WordCategory
        get() = when (this) {
            coreWordList.id -> WordCategory.Core
            else            -> WordCategory.Bonus
        }

    companion object {
        fun WordSourceLoader.getFactotum(gamePlan: GamePlan): Factotum {
            return Factotum(
                coreWordList = getWordList(gamePlan.coreWordList),
                bonusWordLookups = gamePlan.bonusWordLookups.map(this::getWordLookup),
                definitionLookups = allDefinitionLookups
            )
        }
    }
}

data class FoundWord(val word: Word, val source: WordLookup.Id, val category: WordCategory)