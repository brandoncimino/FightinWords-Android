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
}