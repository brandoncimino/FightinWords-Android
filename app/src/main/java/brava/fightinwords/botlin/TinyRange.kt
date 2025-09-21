package brava.fightinwords.botlin

import androidx.collection.IntIntPair
import kotlinx.serialization.Serializable
import kotlin.math.max

/**
 * A [Serializable] version of [IntRange] based on [IntIntPair].
 *
 * TODO: Serializing this as [Long] in JSON almost always results in a much longer string. For example:
 *   TinyRange:     1 til 4
 *   toString():    1..4
 *   Long:          4294967299
 *  UPDATE:
 *      After more experimentation, this is also the case in [Cbor]. Serializing as 2 [Int]s becomes astronomically MORE advantageous vs. 1 [Long]
 *      when the [packed] is refactored to be a pair of [start] + [length] instead of [start] + [endInclusive].
 */
@Serializable
@JvmInline
value class TinyRange(
    /**
     * @see [IntIntPair.packedValue]
     */
    val packed: Long,
) {
    /**
     * @see IntRange.start
     */
    val start: Int get() = (packed shr 32).toInt()

    /**
     * @see IntRange.endInclusive
     */
    val endInclusive: Int get() = (packed and 0xFFFFFFFF).toInt()

    constructor(
        start: Int,
        endInclusive: Int,
    ) : this(packInts(start, endInclusive))

    companion object {
        /**
         * Packs two Int values into one Long value for use in inline classes.
         *
         * @see [androidx.compose.ui.util.packInts]
         * */
        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun packInts(start: Int, endInclusive: Int): Long {
            return (start.toLong() shl 32) or (endInclusive.toLong() and 0xFFFFFFFF)
        }

        infix fun Int.til(endExclusive: Int): TinyRange {
            return TinyRange(this, endExclusive - 1)
        }

        val TinyRange.length
            inline get() = max(endInclusive - start + 1, 0)

        val TinyRange.isEmpty inline get() = packed == empty.packed || endInclusive < start
        val TinyRange.isNotEmpty inline get() = endInclusive >= start

        operator fun TinyRange.iterator() = (start..endInclusive).iterator()

        val empty: TinyRange inline get() = TinyRange(0)
    }

    override fun toString(): String {
        return "$start..$endInclusive"
    }

    fun toIntRange(): IntRange = start..endInclusive
}
