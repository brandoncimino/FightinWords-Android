package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.utf8Bytes
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Word

data class WordDefinition(
    val word: Word,
    val language: KnownLanguage,
    val partOfSpeech: PartOfSpeech?,
    val definition: String,
    val source: DefinitionLookup.Id,
    val annotatedParts: List<AnnotatedDefinitionPart> = listOf(),
)

sealed interface AnnotatedDefinitionPart {

    data class Literal(val text: ByteSlice) : AnnotatedDefinitionPart

    /**
     * ## For [NaspaWordListEntry]:
     * `{brassiere=n}` in `BRA a {brassiere=n} [n BRAS]`
     */
    data class Link(val wordKey: WordKey) : AnnotatedDefinitionPart

    /**
     * ## For [NaspaWordListEntry]:
     * `<duo=n>` in ```DUI <duo=n> [n]```
     */
    data class Inline(val inlineDefinition: WordDefinition) : AnnotatedDefinitionPart

    companion object {
        fun MutableList<AnnotatedDefinitionPart>.appendInline(
            wordKey: WordKey,
            definitionLookup: DefinitionLookup,
        ) {
            val definitionToInline = definitionLookup.findDefinition(wordKey.word)

            when (definitionToInline) {
                null -> add(Literal("⚠️ Definition not found: $wordKey".utf8Bytes())) // TODO: add some kind of error reporting for this
                else -> add(Inline(definitionToInline))
            }
        }
    }
}