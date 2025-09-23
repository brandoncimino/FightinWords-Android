package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.fastSlice
import brava.fightinwords.botlin.getMemoryMappedBuffer
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import com.google.common.base.Stopwatch
import java.io.File

fun NaspaWordList(
    file: File,
): NaspaWordList {
    val stopwatch = Stopwatch.createStarted()
    val nwl = NaspaWordList(file.getMemoryMappedBuffer().fastSlice())
    val elapsed = stopwatch.elapsed()
    println("Loaded $file in $elapsed")
    return nwl
}

fun NaspaWordList(
    bytes: ByteSlice,
): NaspaWordList {
    val index = parseWordLineRanges(
        bytes,
    )

    return NaspaWordList(index, bytes)
}

class NaspaWordList(
    val index: ShortlexWordListIndex,
    private val bytes: ByteSlice,
) : DefinitionLookup, ShortlexWordList {
    override fun getCountOfWordsWithLength(wordLength: Int): Int =
        index.wordLengthCounts[wordLength]

    override val wordCount: Int = run {
        var sum = 0
        index.wordLengthCounts.forEachValue { sum += it }
        sum
    }

    override fun getWordByIndex(wordIndex: Int): Word {
        TODO("Not yet implemented")
    }

    fun findEntry(word: Word): NaspaWordListEntry? {
        return when (word) {
            is TinyWord -> findEntry(word)
            else        -> null
        }
    }

    fun findRawEntry(word: TinyWord): ByteSlice? {
        val range = index.entries.findRange(word)
        return when {
            range.isEmpty -> null
            else          -> bytes.slice(range)
        }
    }

    fun findEntry(word: TinyWord): NaspaWordListEntry? {
        return findRawEntry(word)?.let {
            NaspaWordListEntry.parse(it)
        }
    }

    override fun findDefinition(word: Word): WordDefinition? {
        return findEntry(word)?.toWordDefinition()
    }

    override fun isWord(word: Word): Boolean {
        return when (word) {
            is TinyWord -> isWord(word)
            else        -> false
        }
    }

    fun isWord(word: TinyWord): Boolean {
        return index.entries.containsWord(word)
    }
}

private fun parseWordLineRanges(
    bytes: ByteSlice,
): ShortlexWordListIndex {
    return ShortlexWordListIndex.build(bytes) { lineStart, lineEndInclusive ->
        TinyWord.extractTinyWordFromRange(
            ' '.code.toByte(),
            lineStart,
            lineEndInclusive,
            bytes::get,
            TinyWord.Companion.LongWordHandling.Skip
        )
    }
}