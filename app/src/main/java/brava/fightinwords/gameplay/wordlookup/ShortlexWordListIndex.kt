package brava.fightinwords.gameplay.wordlookup

import androidx.collection.IntIntMap
import androidx.collection.MutableIntIntMap
import androidx.collection.MutableLongLongMap
import androidx.collection.mutableIntIntMapOf
import androidx.collection.mutableLongLongMapOf
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.forEachLineRange
import brava.fightinwords.botlin.serialization.IntIntMapSerializer
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word.Companion.shortlexCompare
import kotlinx.serialization.Serializable

/**
 * Contains information computed from the whole of a word list like [NaspaWordList].
 */
@Serializable
data class ShortlexWordListIndex(
    /**
     * The ranges with a source like [NaspaWordList.bytes] that contain each word list entry.
     */
    val entries: TinyWordRanges,
    @Serializable(IntIntMapSerializer::class)
    val wordLengthCounts: IntIntMap,
) {
    companion object {
        inline fun build(
            bytes: ByteSlice,
            wordExtractor: (lineStart: Int, lineEndInclusive: Int) -> TinyWord,
        ): ShortlexWordListIndex {
            val ranges = mutableLongLongMapOf()
            val wordLengthCounts = mutableIntIntMapOf()

            processLines(bytes, wordExtractor, ranges, wordLengthCounts, stopOnEmptyWord = true)

            return ShortlexWordListIndex(TinyWordRanges(ranges), wordLengthCounts)
        }

        inline fun processLines(
            bytes: ByteSlice,
            wordExtractor: (Int, Int) -> TinyWord,
            ranges: MutableLongLongMap,
            wordLengthCounts: MutableIntIntMap,
            stopOnEmptyWord: Boolean,
        ) {
            var previousWord = TinyWord.empty
            bytes.forEachLineRange { start, endInclusive ->
                if (endInclusive < start) {
                    return@forEachLineRange
                }

                val word = wordExtractor(start, endInclusive)

                // Make sure that we do actually have a shortlex word list
                if (word.shortlexCompare(previousWord) < 0) {
                    throw IllegalStateException("The word `$word` comes before the previous word `$previousWord` in shortlex order, which means that our input is NOT in shortlex order!")
                }
                previousWord = word

                if (word.isEmpty()) {
                    when (stopOnEmptyWord) {
                        true  -> return@processLines
                        false -> return@forEachLineRange
                    }
                }

                ranges.put(word.packed, TinyRange.startEndInclusive(start, endInclusive).packed)
                wordLengthCounts[word.length] = wordLengthCounts.getOrDefault(word.length, 0) + 1
            }
        }
    }

    override fun toString(): String {
        return "${this::class}: wordLengthCounts = $wordLengthCounts"
    }
}
