package brava.fightinwords.gameplay.data

/**
 * Wraps a [String] so that you can treat it as a [List]<[Char]> without unnecessary allocations.
 */
internal class CharList(internal val stringValue: String) : List<Char> {
    override val size: Int = stringValue.length
    override fun contains(element: Char): Boolean = stringValue.contains(element)
    override fun containsAll(elements: Collection<Char>): Boolean = elements.all(stringValue::contains)
    override fun get(index: Int): Char = stringValue[index]
    override fun indexOf(element: Char): Int = stringValue.indexOf(element)
    override fun isEmpty(): Boolean = stringValue.isEmpty()
    override fun iterator(): Iterator<Char> = stringValue.iterator()
    override fun lastIndexOf(element: Char): Int = stringValue.lastIndexOf(element)
    override fun listIterator(): ListIterator<Char> = listIterator(0)
    override fun listIterator(index: Int): ListIterator<Char> = CharListIterator(stringValue, index)

    private class CharListIterator(
        private val stringValue: String,
        private var index: Int
    ) : ListIterator<Char> {
        override fun hasNext(): Boolean = index < stringValue.length
        override fun hasPrevious(): Boolean = index > 0
        override fun next(): Char = stringValue[index++]
        override fun nextIndex(): Int = index + 1
        override fun previous(): Char = stringValue[--index]
        override fun previousIndex(): Int = index - 1
    }

    override fun subList(fromIndex: Int, toIndex: Int): CharList = CharList(stringValue.substring(fromIndex, toIndex))
}