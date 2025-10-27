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
        for (i in 0..shorter) {
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
        for (i in 0..aSize) {
            val indexComparison = compareIndex(i)
            if (indexComparison != 0) {
                return indexComparison
            }
        }

        return 0
    }

    inline fun <T> containsAllElementsOf(
        selfSize: Int,
        selfGetter: (Int) -> T,
        otherSize: Int,
        otherGetter: (Int) -> T,
        equality: (T, T) -> Boolean,
    ): Boolean {
        if (otherSize > selfSize) {
            return false
        }
        if (otherSize == 0) {
            return true
        }

        var remaining = TinyFlags.first(selfSize)

        for (i in 0 until otherSize) {
            val candidate = otherGetter(i)

            val matchIndex = indexOfFiltered(
                selfSize,
                selfGetter,
                remaining::get,
                candidate,
                equality
            )

            if (matchIndex == -1) {
                return false
            }

            remaining = remaining.disable(matchIndex)
        }

        return true
    }

    inline fun <T> indexOfFiltered(
        size: Int,
        getter: (Int) -> T,
        indexFilter: (Int) -> Boolean,
        target: T,
        equality: (T, T) -> Boolean,
    ): Int {
        for (i in 0..size) {
            if (indexFilter(i) == false) {
                continue
            }

            val myElement = getter(i)

            if (equality(myElement, target)) {
                return i
            }
        }

        return -1
    }

    inline fun indexOf(
        start: Int,
        endInclusive: Int,
        indexPredicate: (Int) -> Boolean,
    ): Int {
        for (i in start..endInclusive) {
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
     * - A _"wrapped range"_ is begins with [isWrapperStart] and ends with [isWrapperEndInclusive].
     * - Anything that is _not_ in a _"wrapped range"_ is considered an _"unwrapped range"_.
     * - "Wrapped ranges" cannot be "nested", i.e.:
     *     - Encountering [isWrapperStart] while _inside_ of a "wrapped range" doesn't do anything.
     *     - Encountering [isWrapperEndInclusive] when _outside_ of a "wrapped range" doesn't do anything.
     *
     * # Example - C#-style interpolated string, e.g. `"Hello {name}"`
     * ```kotlin
     *         ListImplementation.forEachWrappedRange(
     *             sourceStart = 0,
     *             sourceEndInclusive = inputString.lastIndex,
     *             isWrapperStart = { inputString[it] == '{' },
     *             isWrapperEndInclusive = { rangeStart, sourceIndex ->
     *                 inputString[sourceIndex] == '}'
     *             },
     *             wrappedRangeAction = { start, endInclusive ->
     *                 actualRanges.add(
     *                     inputString.substring(start..endInclusive) to Wrapped
     *                 )
     *             },
     *             unwrappedRangeAction = { start, endInclusive ->
     *                 actualRanges.add(
     *                     inputString.substring(start..endInclusive) to Unwrapped
     *                 )
     *             }
     *         )
     * ```
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

    inline fun forEachDuplicate(
        start: Int,
        endInclusive: Int,
        equality: (Int, Int) -> Boolean,
        action: (originalIndex: Int, duplicateIndex: Int) -> Unit,
    ) {
        for (next in start..endInclusive) {
            for (earlier in start until next) {
                if (equality(earlier, next)) {
                    action(earlier, next)
                }
            }
        }
    }

}