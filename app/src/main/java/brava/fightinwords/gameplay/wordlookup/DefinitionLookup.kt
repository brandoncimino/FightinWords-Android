package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

/**
 * A source of [WordDefinition]s.
 *
 * # [DefinitionLookup] vs. [WordLookup]
 * The key difference here is that a [WordLookup] has **gameplay implications** -
 */
interface DefinitionLookup {
    /**
     * Finds the "preferred" definition for a given [word].
     *
     * > 📎 Note:
     * > This is not necessarily the [findAllDefinitions]`.first()`.
     */
    fun findDefinition(word: Word): WordDefinition?

    /**
     * Finds all of the definitions I know of for a given [word].
     */
    fun findAllDefinitions(word: Word): Sequence<WordDefinition>

    companion object {
        fun DefinitionLookup.requireDefinition(word: Word): WordDefinition {
            return checkNotNull(findDefinition(word)) {
                "Couldn't find a definition for `$word` in $this!"
            }
        }
    }

    @Serializable
    sealed interface Id
}