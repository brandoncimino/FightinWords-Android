package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import java.io.InputStream
import java.util.function.IntFunction


data class WordPool(
    private val definitions: Map<Word, WordDefinition>
) : WordLookup, DefinitionLookup, Collection<WordDefinition> by definitions.values {
    override fun isWord(word: Word): Boolean = definitions.contains(word)
    override fun findDefinition(word: Word): WordDefinition? = definitions[word]

    @Deprecated("This is a mandatory override of a deprecated Java method")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
        @Suppress("DEPRECATION")
        return super.toArray(generator)
    }

    companion object {
        fun fromDefinitionFile(letterPool: LetterPool, csvStream: InputStream): WordPool {
            return WordPool(
                WordLookupHelpers.parseConstructibleWords(csvStream, KnownLanguage.English, letterPool)
                    .associateBy { it.word })
        }
    }
}