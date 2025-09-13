package brava.fightinwords.botlin

import androidx.compose.runtime.Stable

/**
 * A [range] of characters within another [source].
 */
@ConsistentCopyVisibility
@Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
@Stable
data class Substring private constructor(
    private val source: CharSequence,
    val start: Int,
    /**
     * > 📎 While most of kotlin prefers ranges that [IntRange.endInclusive],
     * this class uses [endExclusive] to match [java.lang.String.substring].
     */
    val endExclusive: Int,
) : CharSequence {
    private var _string: String = ""

    override val length: Int get() = endExclusive - start

    override operator fun get(index: Int): Char {
        return source[start + index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Substring {
        if (endIndex < startIndex) {
            return empty
        }

        if (startIndex == 0 && endIndex == length) {
            return this
        }

        return Substring(
            source,
            start + startIndex,
            start + endIndex - 1
        )
    }

    companion object {
        @JvmStatic
        val empty = Substring("", 0, 0)

        fun CharSequence.fastSubstring(start: Int, endExclusive: Int): CharSequence {
            if (endExclusive <= start) {
                return empty
            }

            if (start == 0 && endExclusive == length) {
                return this
            }

            return Substring(this, start, endExclusive)
        }

        @JvmStatic
        fun CharSequence.fastSlice(start: Int, endInclusive: Int): CharSequence {
            return fastSubstring(start, endInclusive + 1)
        }
    }

    override fun toString(): String {
        if (_string.length == 0) {
            _string = createString()
        }

        return _string
    }

    private fun createString(): String {
        assert { length > 0 }

        val chars = CharArray(length)
        for (i in start until endExclusive) {
            chars[i] = source[i]
        }
        return String(chars)
    }
}