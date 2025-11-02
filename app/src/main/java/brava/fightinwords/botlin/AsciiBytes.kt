package brava.fightinwords.botlin

import brava.fightinwords.botlin.AsciiBytes.Companion.isAscii
import brava.fightinwords.botlin.TinyRange.Companion.endInclusive
import org.jetbrains.annotations.VisibleForTesting

/**
 * A chunk of [bytes] that are known to be in the [java.nio.charset.StandardCharsets.US_ASCII] range,
 * and therefore safely map 1:1 from [Byte.toInt].[toChar][Int.toChar].
 */
@ConsistentCopyVisibility
data class AsciiBytes private constructor(val bytes: ByteSlice) : CharSequence {
    override val length: Int
        get() = bytes.size

    override fun get(index: Int): Char = bytes[index].toInt().toChar()
    fun getByte(index: Int): Byte = bytes[index]

    override fun subSequence(startIndex: Int, endIndex: Int): AsciiBytes {
        return AsciiBytes(
            bytes.slice(
                start = startIndex,
                endInclusive = endIndex - 1
            )
        )
    }

    fun slice(start: Int, endInclusive: Int): AsciiBytes {
        return subSequence(start, endInclusive + 1)
    }

    fun slice(tinyRange: TinyRange): AsciiBytes {
        return slice(tinyRange.start, tinyRange.endInclusive)
    }

    companion object {
        private val Byte.isAscii: Boolean inline get() = this >= 0

        /**
         * @return `true` if all my bytes are in the [Charsets.US_ASCII] range.
         */
        @VisibleForTesting
        internal fun ByteSlice.isAscii(): Boolean {
            for (i in 0 until size) {
                if (get(i).isAscii == false) {
                    return false
                }
            }

            return true
        }

        /**
         * Validates that [this].[isAscii] and wraps it in [AsciiBytes].
         *
         * @throws IllegalArgumentException If [this] contains non-ASCII bytes.
         */
        fun ByteSlice.toAscii(): AsciiBytes {
            require(isAscii()) { "The input contained non-ASCII bytes." }
            return AsciiBytes(this)
        }

        fun ByteSlice.toAsciiUnsafe(): AsciiBytes {
            debugAssert { isAscii() }
            return AsciiBytes(this)
        }
    }

    override fun toString(): String {
        return StringBuilder(length)
            .append(this)
            .toString()
    }
}