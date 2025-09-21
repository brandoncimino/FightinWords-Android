package brava.fightinwords.gameplay.wordlookup

import androidx.collection.LongLongMap
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.serialization.LongLongMapSerializer
import brava.fightinwords.gameplay.data.TinyWord
import kotlinx.serialization.Serializable

/**
 * Interprets the [LongLongMap.keys] and [LongLongMap.values] as [TinyWord]s and [TinyRange]s, respectively.
 */
@Serializable
@JvmInline
value class TinyWordRanges(
    @Serializable(LongLongMapSerializer::class)
    val packedRanges: LongLongMap,
) {
    fun findRange(tinyWord: TinyWord): TinyRange {
        val packed = packedRanges.getOrDefault(tinyWord.packed, TinyRange.empty.packed)
        return TinyRange(packed)
    }

    fun containsWord(tinyWord: TinyWord): Boolean {
        return packedRanges.containsKey(tinyWord.packed)
    }
}