package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word

sealed class MemoryMappedDefinitionLookup(
    protected val entries: LongMappedLines,
) : DefinitionLookup, WordLookup {
    final override fun findDefinition(word: Word): WordDefinition? {
        return when (word) {
            is TinyWord -> findDefinition(word)
            else        -> null
        }
    }

    fun findDefinition(tinyWord: TinyWord): WordDefinition? {
        val line = entries.findLine(tinyWord.packed) ?: return null
        return parseLine(line)
    }

    /**
     * Checks for the presence of [tinyWord] without invoking [parseLine].
     */
    fun containsWord(tinyWord: TinyWord): Boolean {
        return entries.containsKey(tinyWord.packed)
    }

    final override fun isWord(word: Word): Boolean {
        return word is TinyWord && containsWord(word)
    }

    protected abstract fun parseLine(
        rawEntry: ByteSlice,
    ): WordDefinition
}