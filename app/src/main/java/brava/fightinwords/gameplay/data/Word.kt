package brava.fightinwords.gameplay.data

import java.util.function.IntFunction

/**
 * A collection of [Letter]s.
 */
data class Word(val letters: List<Letter>) : List<Letter> by letters {
    private class LetterList(val stringValue: String) : AbstractList<Letter>() {
        override val size: Int = stringValue.length
        override fun get(index: Int): Letter = Letter(stringValue[index])
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
    }

    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> = super.toArray(generator)

    override fun toString(): String {
        return when (this.letters) {
            is LetterList -> this.letters.stringValue
            else -> this.joinToString { it.toString() }
        }
    }
}