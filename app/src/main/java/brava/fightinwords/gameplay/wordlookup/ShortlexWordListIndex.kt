package brava.fightinwords.gameplay.wordlookup

import androidx.collection.IntIntMap
import androidx.collection.LongList
import androidx.collection.mutableIntIntMapOf
import androidx.collection.mutableLongListOf
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.forEachLineRange
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word.Companion.shortlexCompareTo
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min

/**
 * Contains information computed from the whole of a word list like [NaspaWordList].
 */
class ShortlexWordListIndex private constructor(
    /**
     * The ranges within a source like [NaspaWordList.bytes] that contain each word list entry.
     */
    private val entryWords: LongList,
    private val entryRanges: LongList,
    val wordLengthCounts: IntIntMap,
) {
    @Transient
    val wordCount = entryWords.size

    @Transient
    val wordLengthRange = wordLengthCounts.keyRange()

    fun getWordByIndex(wordIndex: Int) = entryWords[wordIndex].toWord()
    fun getRangeByIndex(wordIndex: Int) = entryRanges[wordIndex].toRange()

    fun findWordIndex(tinyWord: TinyWord): Int {
        return entryWords.indexOf(tinyWord.packed)
    }

    fun findWordRange(tinyWord: TinyWord): TinyRange {
        return when (val wordIndex = findWordIndex(tinyWord)) {
            -1   -> TinyRange.empty
            else -> TinyRange(entryRanges[wordIndex])
        }
    }

    companion object {
        fun build(
            bytes: ByteSlice,
            wordExtractor: (lineStart: Int, lineEndInclusive: Int) -> TinyWord,
        ): ShortlexWordListIndex {
            val wordLengthCounts = mutableIntIntMapOf()

            val entryWords = mutableLongListOf()
            val entryRanges = mutableLongListOf()

            processLines(
                bytes,
                wordExtractor,
                { word, range ->
                    entryWords.add(word.packed)
                    entryRanges.add(range.packed)

                    wordLengthCounts[word.length] =
                        wordLengthCounts.getOrDefault(word.length, 0) + 1
                },
                stopOnEmptyWord = true
            )

            return ShortlexWordListIndex(
                entryWords,
                entryRanges,
                wordLengthCounts
            )
        }

        inline fun processLines(
            bytes: ByteSlice,
            wordExtractor: (Int, Int) -> TinyWord,
            forEachRange: (TinyWord, TinyRange) -> Unit,
            stopOnEmptyWord: Boolean,
        ) {
            var previousWord = TinyWord.empty
            bytes.forEachLineRange { start, endInclusive ->
                if (endInclusive < start) {
                    return@forEachLineRange
                }

                val word = wordExtractor(start, endInclusive)

                // Make sure that we do actually have a shortlex word list
                if (word.shortlexCompareTo(previousWord) < 0) {
                    throw IllegalStateException("The word `$word` comes before the previous word `$previousWord` in shortlex order, which means that our input is NOT in shortlex order!")
                }
                previousWord = word

                if (word.isEmpty()) {
                    when (stopOnEmptyWord) {
                        true  -> return@processLines
                        false -> return@forEachLineRange
                    }
                }

                forEachRange(word, TinyRange.startEndInclusive(start, endInclusive))
            }
        }
    }

    override fun toString(): String {
        return "${this::class}: wordLengthCounts = $wordLengthCounts"
    }
}

private fun IntIntMap.keyRange(): IntRange {
    var min = Int.MAX_VALUE
    var max = Int.MIN_VALUE
    forEachKey {
        min = min(min, it)
        max = max(max, it)
    }
    return min..max
}

private fun Long.toWord(): TinyWord = TinyWord(this)
private fun Long.toRange(): TinyRange = TinyRange(this)