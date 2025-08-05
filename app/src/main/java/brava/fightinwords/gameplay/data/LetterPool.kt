package brava.fightinwords.gameplay.data

import kotlinx.serialization.Serializable
import java.util.function.IntFunction

@Serializable
data class LetterPool(val letters: List<Letter>) : List<Letter> by letters {
    @Suppress("unused")
    fun canConstruct(word: Collection<Letter>): Boolean {
        if (word.size > size) {
            return false
        }

        return canConstructInternal(
            word.asSequence().map { it.character },
            CharArray(size)
        ) >= 0
    }

    internal fun copyTo(buffer: CharArray): CharArray {
        for (i in 0 until buffer.size) {
            buffer[i] = this[i].character
        }
        return buffer
    }

    /**
     * @return the length of the [word], or -1 if I can't construct it
     */
    internal fun canConstructInternal(
        word: Sequence<Char>,
        buffer: CharArray,
    ): Int {
        this.copyTo(buffer)

        var length = 0

        for (letter in word) {
            length += 1
            assert(letter != Char.MIN_VALUE)

            val matchIndex = buffer.indexOf(letter)

            if (matchIndex < 0) {
                return -1
            } else {
                buffer[matchIndex] = Char.MIN_VALUE
            }
        }

        return length
    }


    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method.")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
        return super.toArray(generator)
    }
}