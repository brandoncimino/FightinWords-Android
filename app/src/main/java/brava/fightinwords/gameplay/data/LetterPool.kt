package brava.fightinwords.gameplay.data

import java.util.function.IntFunction

data class LetterPool(val letters: List<Letter>) : List<Letter> by letters {
    fun canConstruct(word: Word): Boolean {
        return canConstruct(
            word,
            CharArray(size)
        )
    }

    internal fun canConstruct(
        word: Word,
        buffer: CharArray
    ): Boolean {
        assert(buffer.size >= size)

        if (word.size > size) {
            return false
        }

        for (i in 0 until size) {
            buffer[i] = this[i].character
        }

        for (letter in word) {
            val matchIndex = buffer.indexOf(letter.character)

            if (matchIndex < 0) {
                return false
            } else {
                buffer[matchIndex] = Char.MIN_VALUE
            }
        }

        return true
    }


    @Deprecated("This is a mandatory override of a deprecated Java method.")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
        return super.toArray(generator)
    }
}