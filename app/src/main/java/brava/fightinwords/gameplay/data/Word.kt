package brava.fightinwords.gameplay.data

import kotlinx.serialization.Serializable
import java.util.function.IntFunction
import kotlin.math.min

/**
 * A collection of [Letter]s.
 *
 * TODO: Make [TinyWord] more interchangeable with [Word]. Options include:
 *   - Make [Word] into a `sealed interface`
 */
@Serializable
@JvmInline
value class Word(val letters: List<Letter>) : List<Letter> by letters, Comparable<Word> {
    private class LetterList(val stringValue: String) : AbstractList<Letter>() {
        override val size: Int = stringValue.length
        override fun get(index: Int): Letter = TinyLetter(stringValue[index])
    }

    val length: Int
        get() = size

    companion object {
        fun Iterable<Letter>.toWord(): Word {
            return when (this) {
                is Word -> this
                is List<Letter> -> Word(this)
                else -> Word(this.toList())
            }
        }

        fun String.toWord(): Word {
            return Word(LetterList(this))
        }

        fun Word.tryGetStringValue(): String? = when {
            letters is LetterList -> letters.stringValue
            else                  -> null
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> = super.toArray(generator)

    override fun toString(): String {
        return when (this.letters) {
            is LetterList -> this.letters.stringValue
            else -> this.joinToString(separator = "") { it.toString() }
        }
    }

    override fun compareTo(other: Word): Int {
        if (this.letters is LetterList && other.letters is LetterList) {
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
}