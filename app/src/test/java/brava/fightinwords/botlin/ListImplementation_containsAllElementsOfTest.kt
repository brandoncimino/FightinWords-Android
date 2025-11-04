package brava.fightinwords.botlin

import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import org.assertj.core.api.Assertions
import org.junit.Test

class ListImplementation_containsAllElementsOfTest {
    @Test
    fun yolo() {
        val self = listOf("a", "b", "c")
        val other = listOf("a", "c")

        val result = ListImplementation.containsAllElementsOf(
            self.size,
            other.size,
            { selfIndex, otherIndex -> self[selfIndex] == other[otherIndex] }
        )

        println("result = ${result}")
        Assertions.assertThat(result)
            .isTrue
    }

    @Test
    fun letterPoolCanConstructTest() {
        val letterPool = LetterPool("yolo".toWord())
        val candidateWord = "yo".toWord()

        val letterPoolLetters = letterPool.codePoints

        val actual = letterPool.canConstruct(candidateWord)

        Assertions.assertThat(actual)
            .isTrue
    }
}

