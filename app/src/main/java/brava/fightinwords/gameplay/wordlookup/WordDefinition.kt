package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

@Serializable
data class WordDefinition(
    val word: Word,
    val language: KnownLanguage,
    val partOfSpeech: String?,
    val definition: String,
    val isNaspaWord: Boolean,
    val substitutions: List<WordDefinitionSubstitution> = listOf()
)

sealed interface WordDefinitionSubstitution {
    val replacementRange: IntRange
    val word: Word

    /**
     * ## For [NaspaWordListEntry]:
     * `<duo=n>` in ```DUI <duo=n> [n]```
     */
    data class Inline(
        override val replacementRange: IntRange,
        override val word: Word,
    ) : WordDefinitionSubstitution

    /**
     * ## For [NaspaWordListEntry]:
     * `{brassiere=n}` in `BRA a {brassiere=n} [n BRAS]`
     */
    data class Link(
        override val replacementRange: IntRange,
        override val word: Word,
    ) : WordDefinitionSubstitution
}