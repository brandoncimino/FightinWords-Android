package brava.fightinwords.gameplay.data

import java.util.function.IntFunction

data class LetterPool(val letters: List<Letter>) : List<Letter> by letters {
    fun canConstruct(word: Collection<Letter>): Boolean {
        if (word.size > size) {
            return false
        }

        return canConstructInternal(
            word.asSequence().map { it.character },
            CharArray(size)
        )
    }

    internal fun canConstructInternal(
        word: CharSequence,
        buffer: CharArray
    ): Boolean {
        if (word.length > size) {
            return false
        }

        return canConstructInternal(
            word.asSequence(),
            buffer
        )
    }

    internal fun copyTo(buffer: CharArray): CharArray {
        assert(buffer.size >= size)
        for (i in 0 until size) {
            buffer[i] = this[i].character
        }
        return buffer
    }

    internal fun canConstructInternal(
        word: Sequence<Char>,
        buffer: CharArray
    ): Boolean {
        this.copyTo(buffer)

        for (letter in word) {
            assert(letter != Char.MIN_VALUE)

            val matchIndex = buffer.indexOf(letter)

            if (matchIndex < 0) {
                return false
            } else {
                buffer[matchIndex] = Char.MIN_VALUE
            }
        }

        return true
    }


    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method.")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
        return super.toArray(generator)
    }
}