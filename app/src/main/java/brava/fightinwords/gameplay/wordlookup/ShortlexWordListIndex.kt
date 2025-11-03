package brava.fightinwords.gameplay.wordlookup

import androidx.collection.IntIntMap
import androidx.collection.LongList
import androidx.collection.mutableIntIntMapOf
import androidx.collection.mutableLongListOf
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.blog
import brava.fightinwords.botlin.forEachLineRange
import brava.fightinwords.botlin.toUtf8String
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word.Companion.shortlexCompareTo
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min

/**
 * Contains information computed from the whole of a word list like [NaspaWordList].
 */
open class WordListIndex(
    /**
     * The ranges within a source like [NaspaWordList.bytes] that contain each word list entry.
     */
    private val entryWords: LongList,
    private val entryRanges: LongList,
    val wordLengthCounts: IntIntMap,
) {
    init {
        require(
            entryWords.size == entryRanges.size
            && entryWords.size == wordLengthCounts.valueSum()
        ) {
            "We must have the same number of `entryWords` (${entryWords.size}), `entryRanges` (${entryRanges.size}), and total `wordLengthCounts` (${wordLengthCounts.valueSum()})!"
        }
    }

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

    enum class LoopAction {
        Skip,
        Break,
        Error
    }

    companion object {
        fun build(
            bytes: ByteSlice,
            stopOnEmptyWord: Boolean,
            wordExtractor: (lineStart: Int, lineEndInclusive: Int) -> TinyWord,
        ): WordListIndex {
            val wordLengthCounts = mutableIntIntMapOf()

            val entryWords = mutableLongListOf()
            val entryRanges = mutableLongListOf()

            val isShortlex = processLines(
                bytes,
                wordExtractor,
                { word, range ->
                    entryWords.add(word.packed)
                    entryRanges.add(range.packed)

                    wordLengthCounts[word.length] =
                        wordLengthCounts.getOrDefault(word.length, 0) + 1
                },
                stopOnEmptyWord = stopOnEmptyWord
            )

            return when (isShortlex) {
                true  -> ShortlexWordListIndex(
                    entryWords,
                    entryRanges,
                    wordLengthCounts
                )

                false -> WordListIndex(entryWords, entryRanges, wordLengthCounts)
            }
        }

        inline fun processLines(
            entireWordList: ByteSlice,
            wordExtractor: (Int, Int) -> TinyWord,
            forEachRange: (TinyWord, TinyRange) -> Unit,
            stopOnEmptyWord: Boolean,
        ): Boolean {
            var isShortlex = true
            var previousWord = TinyWord.empty
            entireWordList.forEachLineRange { lineStart, lineEndInclusive, lineIndex ->
                if (lineEndInclusive < lineStart) {
                    return@forEachLineRange
                }

                val word = runCatching { wordExtractor(lineStart, lineEndInclusive) }
                    .getOrElse {
                        val lineBytes = entireWordList.slice(lineStart, lineEndInclusive)
                        throw IllegalStateException(
                            """
                            Failed to extract a ${TinyWord::class.simpleName} from the start of the following range:
                                line range: ${lineBytes.rangeInSource}
                                line bytes: ${lineBytes.indices.map { lineBytes[it] }}
                                line utf8: ${lineBytes.toUtf8String()}
                            """, it
                        )
                    }

                if (word.isEmpty()) {
                    when (stopOnEmptyWord) {
                        true -> return@processLines isShortlex
                        false -> return@forEachLineRange
                    }
                }

                // Make sure that we do actually have a shortlex word list
                if (isShortlex && word.shortlexCompareTo(previousWord) < 0) {
                    blog { "The word `$word` on line ${lineIndex + 1} comes before the previous word `$previousWord` in shortlex order, which means that our input is NOT in shortlex order!" }
                    isShortlex = false
                }
                previousWord = word

                forEachRange(word, TinyRange.startEndInclusive(lineStart, lineEndInclusive))
            }

            return isShortlex
        }
    }

    override fun toString(): String {
        return "${this::class}: wordLengthCounts = $wordLengthCounts"
    }
}

class ShortlexWordListIndex(
    entryWords: LongList,
    entryRanges: LongList,
    wordLengthCounts: IntIntMap,
) : WordListIndex(
    entryWords = entryWords,
    entryRanges = entryRanges,
    wordLengthCounts = wordLengthCounts
)

private fun IntIntMap.keyRange(): IntRange {
    var min = Int.MAX_VALUE
    var max = Int.MIN_VALUE
    forEachKey {
        min = min(min, it)
        max = max(max, it)
    }
    return min..max
}

private fun IntIntMap.valueSum(): Int {
    var total = 0
    forEachValue { total += it }
    return total
}

private fun Long.toWord(): TinyWord = TinyWord(this)
private fun Long.toRange(): TinyRange = TinyRange(this)