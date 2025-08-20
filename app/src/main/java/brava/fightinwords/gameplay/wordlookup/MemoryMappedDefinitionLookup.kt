package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.TinyWord.Companion.toTinyWord
import brava.fightinwords.gameplay.data.Word
import java.nio.ByteBuffer

sealed class MemoryMappedDefinitionLookup(
    private val entries: LongMappedLines,
) : DefinitionLookup {
    final override fun findDefinition(word: Word): WordDefinition? {
        val tinyWord = word.toTinyWord()
        return findDefinition(tinyWord)
    }

    fun findDefinition(tinyWord: TinyWord): WordDefinition? {
        val line = entries.getLine(tinyWord.packed) ?: return null
        return parseLine(line)
    }

    protected abstract fun parseLine(
        line: ByteBuffer,
    ): WordDefinition
}