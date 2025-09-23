package brava.fightinwords.botlin

import androidx.collection.IntIntPair
import brava.fightinwords.botlin.TinyRange.Companion.packInts
import brava.fightinwords.botlin.TinyRange.Companion.unpackFirst
import brava.fightinwords.botlin.TinyRange.Companion.unpackSecond
import kotlinx.serialization.Serializable

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
    val start: Int inline get() = packed.unpackFirst()

    /**
     * The [Collection.size] of the range, i.e. [IntRange.endExclusive] - [IntRange.start].
     */
    val length: Int inline get() = packed.unpackSecond()

    companion object {
        fun startLength(start: Int, length: Int): TinyRange {
            return TinyRange(packInts(start, length))
        }

        fun startEndInclusive(start: Int, endInclusive: Int): TinyRange {
            return startLength(start, endInclusive - start + 1)
        }

        /**
         * Packs two Int values into one Long value for use in inline classes.
         *
         * @param first Unpacked via [unpackFirst]
         * @param second Unpacked via [unpackSecond]
         * @see [androidx.compose.ui.util.packInts]
         * */
        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun packInts(first: Int, second: Int): Long {
            return (first.toLong() shl 32) or (second.toLong() and 0xFFFFFFFF)
        }

        /**
         * Extracts the first [Int] from a [packInts] value.
         *
         * @see [androidx.compose.ui.util.unpackInt1]
         */
        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun Long.unpackFirst(): Int {
            return (this shr 32).toInt()
        }

        /**
         * Extracts the second [Int] from a [packInts] value.
         *
         * @see [androidx.compose.ui.util.unpackInt2]
         */
        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        @PublishedApi
        internal inline fun Long.unpackSecond(): Int {
            return (this and 0xFFFFFFFF).toInt()
        }

        infix fun Int.til(endExclusive: Int): TinyRange {
            return startEndInclusive(this, endExclusive - 1)
        }

        val TinyRange.isEmpty inline get() = packed == empty.packed || endInclusive < start
        val TinyRange.isNotEmpty inline get() = endInclusive >= start

        operator fun TinyRange.iterator() = (start..endInclusive).iterator()

        val empty: TinyRange inline get() = TinyRange(0)

        /**
         * @see IntRange.endInclusive
         */
        val TinyRange.endInclusive: Int inline get() = start + length - 1
    }

    override fun toString(): String {
        return "$start..$endInclusive"
    }

    fun toIntRange(): IntRange = start..endInclusive
}
