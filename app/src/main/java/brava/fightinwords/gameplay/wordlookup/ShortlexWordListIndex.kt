package brava.fightinwords.gameplay.wordlookup

import androidx.collection.IntIntMap
import androidx.collection.mutableIntIntMapOf
import androidx.collection.mutableLongLongMapOf
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.forEachLineRange
import brava.fightinwords.botlin.serialization.IntIntMapSerializer
import brava.fightinwords.gameplay.data.TinyWord
import kotlinx.serialization.Serializable

/**
 * Contains information computed from the whole of a word list like [NaspaWordList].
 *
 * TODO: Theoretically, this can be stored in a separate file and parsed instead of the entire [NaspaWordList]. Is that really necessary, though?
 */
@Serializable
class ShortlexWordListIndex(
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

            bytes.forEachLineRange { start, endInclusive ->
                val word = wordExtractor(start, endInclusive)
                ranges.put(word.packed, TinyRange(start, endInclusive).packed)
                wordLengthCounts[word.length] = wordLengthCounts.getOrDefault(word.length, 0) + 1
            }

            return ShortlexWordListIndex(TinyWordRanges(ranges), wordLengthCounts)
        }
    }
}
