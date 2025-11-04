package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.asciiBytes
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import org.assertj.core.api.Assertions
import org.junit.Test

class FindConstructibleWordsTest {
    private val rawNaspaWordList = """
        EAT to consume food [v ATE, EATEN, EATEN, EATING, EATS, ET] : EATER [n]
        CHOW to {eat=v} [v CHOWED, CHOWING, CHOWS]
        EATS <eat=v> [v]
        EATER one that {eats=v} [n EATERS]
        EXTRA I don't have any substitutions [n]
    """.trimIndent()

    @Test
    fun findConstructibleWords() {
        val nwl = NaspaWordList(rawNaspaWordList.asciiBytes())

        val letterPool = LetterPool("eaterz".toWord())

        val range = nwl.getRangeOfWordsWithLength(
            3,
            4
        )

        Assertions.assertThat(range)
            .isEqualTo(0..2)

        val wordsInRange = range.map { nwl.getWordByIndex(it) }

        Assertions.assertThat(wordsInRange)
            .containsExactly(
                "eat".toWord(),
                "chow".toWord(),
                "eats".toWord()
            )

        Assertions.assertThat(letterPool.canConstruct("eat".toWord()))
            .isTrue

        val constructibleWords = nwl.findConstructibleWords(
            letterPool,
            3,
            99
        )

        Assertions.assertThat(constructibleWords)
            .containsExactlyInAnyOrderElementsOf(
                listOf("eat", "eater")
                    .map { it.toWord() }
            )
    }
}