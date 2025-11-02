package brava.fightinwords.gameplay.data

import brava.fightinwords.gameplay.data.Word.Companion.indices
import org.assertj.core.api.Assertions
import org.junit.Test

class TinyWordTest {
    @Test
    fun tryCreateTooLongReturnsNull() {
        val longString = "a".repeat(TinyWord.MAX_PACK + 1)
        val actualWord = TinyWord.tryCreate(
            { longString.get(it).code },
            start = 0,
            endInclusive = longString.lastIndex
        )

        Assertions.assertThat(actualWord).isNull()

        val shorterWord = TinyWord.tryCreate(
            { longString[it].code },
            start = 0,
            endInclusive = TinyWord.MAX_PACK - 1
        )!!

        Assertions.assertThat(shorterWord)
            .isNotNull
            .extracting { it.length }
            .isEqualTo(TinyWord.MAX_PACK)

        val shorterWordLetters = shorterWord.indices.map { shorterWord[it] }
        Assertions.assertThat(shorterWordLetters)
            .allMatch {
                it == TinyLetter.create('a')
            }
    }

    @Test
    fun tinyLetterCreateUnsafeReallyIsUnsafe() {
        val notTinyLetter = '0'.code.toByte()

        Assertions.assertThatCode {
            TinyLetter.create(notTinyLetter)
        }
            .isInstanceOf(IllegalArgumentException::class.java)

        Assertions.assertThatCode {
            TinyLetter.createUnsafe(notTinyLetter)
        }
            .isNull()

        val unsafely = TinyLetter.createUnsafe(notTinyLetter)
        println("unsafely = ${unsafely}")
    }
}