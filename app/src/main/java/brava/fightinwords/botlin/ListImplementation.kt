package brava.fightinwords.botlin

internal object ListImplementation {
    private inline fun <E> createList(
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

    fun <T : Comparable<T>> compareElements(
        a: List<T>,
        b: List<T>,
    ): Int {
        if (a === b) {
            return 0
        }

        val lengthCompare = a.size.compareTo(b.size)

        val shorter = when {
            lengthCompare < 0 -> a.size
            else              -> b.size
        }

        for (i in 0 until shorter) {
            val comparison = a[i].compareTo(b[i])
            if (comparison != 0) {
                return comparison
            }
        }

        return lengthCompare
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
}