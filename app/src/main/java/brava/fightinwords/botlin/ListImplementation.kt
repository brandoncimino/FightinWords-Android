package brava.fightinwords.botlin

import kotlin.math.min

@PublishedApi
internal object ListImplementation {
    inline fun <E> createList(
        size: Int,
        crossinline getter: (Int) -> E,
    ): AbstractList<E> {
        return object : AbstractList<E>() {
            override val size: Int
                get() = size

            override fun get(index: Int): E {
                return getter(index)
            }
        }
    }

    inline fun <E> createListIterator(
        size: Int,
        crossinline getter: (Int) -> E,
        startIndex: Int = 0,
    ): ListIterator<E> {
        return createList(size, getter)
            .listIterator(startIndex)
    }

    inline fun <E> createSubList(
        size: Int,
        crossinline getter: (Int) -> E,
        start: Int,
        endExclusive: Int,
    ): List<E> {
        return createList(size, getter)
            .subList(start, endExclusive)
    }

    inline fun lexicographicalCompare(
        aSize: Int,
        bSize: Int,
        compareIndex: (index: Int) -> Int,
    ): Int {
        val shorter = min(aSize, bSize)
        for (i in 0 until shorter) {
            val indexComparison = compareIndex(i)
            if (indexComparison != 0) {
                return indexComparison
            }
        }

        return aSize.compareTo(bSize)
    }

    inline fun shortlexCompare(
        aSize: Int,
        bSize: Int,
        compareIndex: (index: Int) -> Int,
    ): Int {
        val sizeCompare = aSize.compareTo(bSize)
        if (sizeCompare != 0) {
            return sizeCompare
        }

        // We could call `lexicographicalCompare` here, but that would incur a redundant `min` call
        for (i in 0 until aSize) {
            val indexComparison = compareIndex(i)
            if (indexComparison != 0) {
                return indexComparison
            }
        }

        return 0
    }

    /**
     * 📎 This could theoretically split [selfSize] and [otherSize] into `start` and `endInclusive` arguments like
     * many other methods in here do, but it actually makes it considerably messier to invoke in scenarios where
     * you don't have a nice [kotlin.collections.lastIndex] methods to take advantage of.
     *
     * You can always achieve the same behavior by adding an offset to the indices inside of the [equality] block.
     */
    inline fun containsAllElementsOf(
        selfSize: Int,
        otherSize: Int,
        equality: (selfIndex: Int, otherIndex: Int) -> Boolean,
    ): Boolean {
        if (otherSize <= 0) {
            return true
        }

        if (otherSize > selfSize) {
            return false
        }

        var remaining = TinyFlags.first(selfSize)

        for (otherIndex in 0 until otherSize) {
            val matchIndex = indexOf(
                0,
                selfSize - 1,
                { selfIndex ->
                    equality(selfIndex, otherIndex)
                },
                remaining::get
            )

            if (matchIndex < 0) {
                return false
            }

            remaining = remaining.disable(matchIndex)
        }

        return true
    }

    inline fun indexOf(
        start: Int,
        endInclusive: Int,
        indexPredicate: (Int) -> Boolean,
        indexFilter: (Int) -> Boolean = { true },
    ): Int {
        for (i in start..endInclusive) {
            if (indexFilter(i) == false) {
                continue
            }

            if (indexPredicate(i)) {
                return i
            }
        }
        return -1
    }

    /**
     * In order, processes "wrapped" and "unwrapped" ranges of a _"`source`"_.
     * > 📎 This method has a "make-believe" _`source`_ parameter, which is not passed explicitly in order to avoid any boxing that might get caused by using generics.
     *
     * - A _"wrapped range"_ begins where [isWrapperStart] is `true` and ends when [isWrapperEndInclusive] is `true`.
     * - [isWrapperEndInclusive] depends on the corresponding [isWrapperStart] index.
     * - Anything that is _not_ in a _"wrapped range"_ is considered an _"unwrapped range"_.
     * - "Wrapped ranges" cannot be "nested", i.e.:
     *     - Encountering [isWrapperStart] while _inside_ of a "wrapped range" doesn't do anything.
     *     - Encountering [isWrapperEndInclusive] when _outside_ of a "wrapped range" doesn't do anything.
     *
     * @sample forEachWrappedRange_cSharpStyleInterpolatedString_sample
     *
     * @param sourceStart The [IntRange.first] index within the _"`source`"_ that should be processed.
     * @param sourceEndInclusive The [IntRange.last] index within the _"`source`"_ that should be processed.
     * @param isWrapperStart Determines if a given index within the _"`source`"_ should be considered the [IntRange.first] of a "wrapped range".
     * @param isWrapperEndInclusive Determines if a given index within the _"`source`"_ should be considered the [IntRange.endInclusive] of a "wrapped range".
     * @param wrappedRangeAction Processes the [IntRange.start] and [IntRange.endInclusive] of each "wrapped range", _*including the indices that satisfied [isWrapperStart] and [isWrapperEndInclusive]*_.
     * @param unwrappedRangeAction Processes the [IntRange.start] and [IntRange.endInclusive] of each "unwrapped range".
     */
    inline fun forEachWrappedRange(
        sourceStart: Int,
        sourceEndInclusive: Int,

        isWrapperStart: (sourceIndex: Int) -> Boolean,
        isWrapperEndInclusive: (rangeStart: Int, sourceIndex: Int) -> Boolean,

        wrappedRangeAction: (start: Int, endInclusive: Int) -> Unit,
        unwrappedRangeAction: (start: Int, endInclusive: Int) -> Unit,
    ) {
        var inRange = false
        var rangeStart = 0

        for (i in sourceStart..sourceEndInclusive) {
            when {
                !inRange -> {
                    //region start new range
                    if (isWrapperStart(i)) {
                        // Process the now-ending unwrapped range
                        processNonEmpty(
                            rangeStart,
                            i - 1,
                            unwrappedRangeAction
                        )

                        // Start the new wrapped range
                        rangeStart = i
                        inRange = true
                        continue
                    }
                    //endregion
                }

                else     -> {
                    //region finish the current range
                    if (isWrapperEndInclusive(rangeStart, i)) {
                        // No need to check for the end of the wrapped range, because the wrapper markers themselves are inclusive
                        wrappedRangeAction(rangeStart, i)
                        rangeStart = i + 1
                        inRange = false
                        continue
                    }
                    //endregion
                }

            }
        }

        // Handle the end of the source, which is always considered to be unwrapped
        processNonEmpty(
            rangeStart,
            sourceEndInclusive,
            unwrappedRangeAction
        )
    }

    @PublishedApi
    internal inline fun processNonEmpty(
        start: Int,
        endInclusive: Int,
        action: (start: Int, endInclusive: Int) -> Unit,
    ) {
        if (endInclusive >= start) {
            action(start, endInclusive)
        }
    }

    /**
     * ```kotlin
     * [a, b, c] =>
     * ab
     * ac
     * bc
     * ```
     */
    inline fun forEachUnorderedPair(
        start: Int,
        endInclusive: Int,
        action: (Int, Int) -> Unit,
    ) {
        for (a in start..endInclusive) {
            for (b in (a + 1)..endInclusive) {
                action(a, b)
            }
        }
    }

    inline fun forEachConsecutivePair(
        start: Int,
        endInclusive: Int,
        action: (Int, Int) -> Unit,
    ) {
        for (first in start until endInclusive) {
            action(first, start)
        }
    }

}

@Suppress("unused", "FunctionName")
private fun forEachWrappedRange_cSharpStyleInterpolatedString_sample() {
    val inputString = "Today is {dayOfWeek}"
    val namedValues = mapOf("dayOfWeek" to "Monday")

    val stringBuilder = StringBuilder()

    ListImplementation.forEachWrappedRange(
        sourceStart = 0,
        sourceEndInclusive = inputString.lastIndex,
        isWrapperStart = { inputString[it] == '{' },
        isWrapperEndInclusive = { rangeStart, sourceIndex ->
            inputString[sourceIndex] == '}'
        },
        wrappedRangeAction = { wrapperStart, wrapperEndInclusive ->
            val nameStart = wrapperStart + 1
            val name = inputString.substring(nameStart until wrapperEndInclusive)

            stringBuilder.append(namedValues[name])
        },
        unwrappedRangeAction = { start, endInclusive ->
            stringBuilder.append(inputString, start..endInclusive)
        }
    )

    assert(stringBuilder.toString() == "Today is Monday")
}


/**
 * This is basically [androidx.compose.ui.util.fastForEach], but:
 * - Doesn't infect the code with [android] or [androidx] dependencies.
 * - Doesn't require that you first cast to [List], which the silly [androidx.compose.ui.util.fastForEach] does because they don't trust
 * all [List] implementations to be [RandomAccess] _(and apparently also don't trust every random-access [List] to be [RandomAccess], either)_.
 *
 * Regarding the trust factor - I find the [RandomAccess] interface quite redundant;
 * [List.get] is the _entire difference_ between [List] and [Collection].
 *
 * I think it is more likely that someone _(e.g. me)_ forgot to implement [RandomAccess] on a [List] that deserves it
 * than it is that they implemented [List] for something that shouldn't use [List.get].
 *
 * This seems to align with [androidx.compose.ui.util.fastForEach], given that they also don't require the [RandomAccess] interface.
 */
inline fun <T> Iterable<T>.smartForEach(
    action: (T) -> Unit,
) {
    when (this) {
        is List<T> -> {
            for (i in indices) {
                action(get(i))
            }
        }

        else       -> forEach(action)
    }
}