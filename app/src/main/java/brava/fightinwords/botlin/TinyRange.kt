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
            get() = max(endInclusive - start + 1, 0)

        val TinyRange.isEmpty get() = length <= 0
        val TinyRange.isNotEmpty get() = !isEmpty

        operator fun TinyRange.iterator() = (start..endInclusive).iterator()

        val empty: TinyRange = TinyRange(0, -1)
    }

    override fun toString(): String {
        return "$start..$endInclusive"
    }

    fun toIntRange(): IntRange = start..endInclusive
}
