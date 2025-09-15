package brava.fightinwords.gameplay.wordlookup

import androidx.collection.IntIntMap
import androidx.collection.MutableIntIntMap
import androidx.collection.mutableIntIntMapOf
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.wordlookup.LongMappedLines.Companion.getMemoryMappedBuffer
import java.io.File
import java.nio.ByteBuffer

fun NaspaWordList(
    file: File,
): NaspaWordList {
    return NaspaWordList(file.getMemoryMappedBuffer())
}

fun NaspaWordList(
    bytes: ByteBuffer,
): NaspaWordList {
    val wordLengthCounts = mutableIntIntMapOf()

    val longMappedLines = NaspaWordList.parseWordLineRanges(
        bytes,
        wordLengthCounts
    )

    return NaspaWordList(longMappedLines, wordLengthCounts)
}

class NaspaWordList internal constructor(
    entries: LongMappedLines,
    wordLengthCounts: IntIntMap,
) : WordMappedLines(entries, wordLengthCounts), ShortlexWordList {
    override fun parseLine(
        rawEntry: ByteSlice,
    ): WordDefinition {
        return NaspaWordListEntry.parse(rawEntry).toWordDefinition()
    }

    override fun getCountOfWordsWithLength(wordLength: Int): Int = wordLengthCounts[wordLength]

    override val wordCount: Int
        get() = TODO("Not yet implemented")

    override fun getWordByIndex(wordIndex: Int): Word {
        TODO("Not yet implemented")
    }

    fun findEntry(word: TinyWord): NaspaWordListEntry? {
        val line = entries.findLine(word.packed)
        return line?.let {
            NaspaWordListEntry.parse(it)
        }
    }

    companion object {
        private const val spaceByte: Byte = ' '.code.toByte()

        internal fun parseWordLineRanges(
            bytes: ByteBuffer,
            wordLengthCounts: MutableIntIntMap,
        ): LongMappedLines {
            return LongMappedLines.create(
                bytes
            ) { buffer, lineStart, lineEndInclusive ->
                val word = TinyWord.extractTinyWordFromRange(
                    spaceByte,
                    lineStart,
                    lineEndInclusive,
                    buffer::get
                )

                wordLengthCounts[word.length] = wordLengthCounts.getOrDefault(word.length, 0) + 1
                return@create word.packed
            }
        }
    }
}

sealed class WordMappedLines(
    entries: LongMappedLines,
    val wordLengthCounts: IntIntMap,
) : MemoryMappedDefinitionLookup(entries), ShortlexWordList {
    companion object {
        inline fun parseWordLineRanges(
            file: File,
            wordLengthCounts: MutableIntIntMap,
            wordExtractor: (buffer: ByteBuffer, lineStart: Int, lineEndInclusive: Int) -> TinyWord,
        ): LongMappedLines {
            return LongMappedLines.create(file) { buffer, lineStart, lineEndInclusive ->
                val word = wordExtractor(buffer, lineStart, lineEndInclusive)
                wordLengthCounts[word.length] = wordLengthCounts.getOrDefault(word.length, 0) + 1
                return@create word.packed
            }
        }
    }
}