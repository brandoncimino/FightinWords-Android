package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus
import java.util.function.IntFunction
import kotlin.math.min

sealed interface Word : List<Letter>, Comparable<Word> {
    val length: Int

    @Deprecated(
        message = "Use `length` for consistency.",
        replaceWith = ReplaceWith("length"),
        level = DeprecationLevel.HIDDEN
    )
    override val size
        @ApiStatus.NonExtendable
        get() = length

    override fun isEmpty(): Boolean = length == 0

    override fun contains(element: Letter): Boolean {
        return asSequence().contains(element)
    }

    override fun containsAll(elements: Collection<Letter>): Boolean {
        return elements.all { contains(it) }
    }

    override fun iterator(): Iterator<Letter> {
        return object : AbstractIterator<Letter>() {
            private var pos: Int = -1
            override fun computeNext() {
                pos += 1

                if (pos < length) {
                    setNext(get(pos))
                } else {
                    done()
                }
            }
        }
    }

    override fun listIterator(): ListIterator<Letter> {
        return listIterator(0)
    }

    companion object {
        fun Iterable<Letter>.toWord(): Word {
            return when (this) {
                is Word -> this
                is List<Letter> -> WordLetters(this)
                else            -> WordLetters(this.toList())
            }
        }

        fun String.toWord(): Word {
            return StringWord(this)
        }
    }

    override fun compareTo(other: Word): Int {
        return ListImplementation.compareElements(this, other)
    }

    override fun listIterator(index: Int): ListIterator<Letter> {
        return ListImplementation.createListIterator(
            length,
            this::get,
            index
        )
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<Letter> {
        return ListImplementation.createSubList(
            length,
            this::get,
            fromIndex,
            toIndex
        )
    }

    override fun indexOf(element: Letter): Int {
        return indices.first { get(it) == element }
    }

    override fun lastIndexOf(element: Letter): Int {
        return indices.last { get(it) == element }
    }
}


@JvmInline
value class StringWord(val stringValue: String) : Word {
    override val length: Int
        get() = stringValue.length

    override fun get(index: Int): Letter {
        return stringValue.codePoints()
            .skip(index.toLong())
            .findFirst()
            .orElseThrow()
            .toLetter()
    }

    override fun toString(): String {
        return stringValue
    }
}

private class LettersString(val stringValue: String) : AbstractList<Letter>() {
    override val size: Int = stringValue.length
    override fun get(index: Int): Letter = TinyLetter(stringValue[index])
}

/**
 * A collection of [Letter]s.
 *
 * TODO: Make [TinyWord] more interchangeable with [Word]. Options include:
 *   - Make [Word] into a `sealed interface`
 */
@Serializable
@JvmInline
value class WordLetters(val letters: List<Letter>) : List<Letter> by letters, Comparable<Word>,
                                                     Word {

    override val length: Int
        get() = size

    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> =
        super<List>.toArray(generator)

    override fun toString(): String {
        return when (this.letters) {
            is LettersString -> this.letters.stringValue
            else             -> this.joinToString(separator = "") { it.toString() }
        }
    }

    fun compareTo(other: WordLetters): Int {
        if (this.letters is LettersString && other.letters is LettersString) {
            return this.letters.stringValue.compareTo(other.letters.stringValue)
        }

        val shorter = min(this.length, other.length)

        for (i in 0 until shorter) {
            val comparison = this.letters[i].compareTo(other.letters[i])
            if (comparison != 0) {
                return comparison
            }
        }

        return 0
    }

    override fun containsAll(elements: Collection<Letter>): Boolean {
        return letters.containsAll(elements)
    }

    override fun indexOf(element: Letter): Int {
        return letters.indexOf(element)
    }

    override fun lastIndexOf(element: Letter): Int {
        return letters.lastIndexOf(element)
    }

    override fun isEmpty(): Boolean {
        return letters.isEmpty()
    }

    override fun listIterator(): ListIterator<Letter> {
        return letters.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<Letter> {
        return letters.listIterator(index)
    }

    override fun contains(element: Letter): Boolean {
        return letters.contains(element)
    }

    override fun iterator(): Iterator<Letter> {
        return letters.iterator()
    }

    override val size: Int
        get() = letters.size

    override fun subList(fromIndex: Int, toIndex: Int): List<Letter> {
        return letters.subList(fromIndex, toIndex)
    }
}