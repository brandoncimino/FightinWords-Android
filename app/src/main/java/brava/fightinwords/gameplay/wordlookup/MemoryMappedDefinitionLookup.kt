package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import java.nio.ByteBuffer

sealed class MemoryMappedDefinitionLookup(
    private val entries: LongMappedLines,
) : DefinitionLookup, WordLookup {
    final override fun findDefinition(word: Word): WordDefinition? {
        return when (word) {
            is TinyWord -> findDefinition(word)
            else        -> null
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun findDefinition(tinyWord: TinyWord): WordDefinition? {
        val line = entries.getLine(tinyWord.packed) ?: return null
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
        line: ByteBuffer,
    ): WordDefinition
}