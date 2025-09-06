package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.annotation.RequiresApi
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

    protected abstract fun parseLine(
        line: ByteBuffer,
    ): WordDefinition
}