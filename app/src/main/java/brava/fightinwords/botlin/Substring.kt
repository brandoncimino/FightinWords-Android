package brava.fightinwords.botlin

/**
 * A [range] of characters within another [source].
 */
@Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
data class Substring(
    val source: CharSequence,
    val range: IntRange,
) : CharSequence {
    private var _sliced: String? = null

    override val length: Int get() = range.endInclusive - range.start + 1

    override operator fun get(index: Int): Char {
        return source[range.start + index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Substring {
        return Substring(
            source,
            range.start + startIndex until range.start + endIndex
        )
    }

    companion object {
        @JvmStatic
        val EMPTY = Substring("", IntRange.EMPTY)

        @JvmStatic
        operator fun CharSequence.get(range: IntRange): Substring {
            return subSlice(range.start, range.endInclusive)
        }

        //        @JvmStatic
        internal operator fun CharSequence.get(range: TinyRange): Substring {
            return subSlice(range.start, range.endInclusive)
        }

        @JvmStatic
        fun CharSequence.subSlice(start: Int, endInclusive: Int): Substring {
            if (endInclusive <= start) {
                return EMPTY
            }

            return Substring(this, start..endInclusive)
        }
    }

    override fun toString(): String {
        if (_sliced == null) {
            _sliced = source.substring(range)
        }

        return _sliced as String
    }
}