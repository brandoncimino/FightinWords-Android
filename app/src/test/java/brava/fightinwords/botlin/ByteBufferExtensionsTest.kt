package brava.fightinwords.botlin

import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntryTest.Companion.utf8Bytes
import org.assertj.core.api.Assertions
import org.junit.Test

class ByteBufferExtensionsTest {
    data class IndexOfScenario(
        private val asciiString: String,
        val target: Char,
        val expectedIndex: Int
    ) {
        val bytes get() = asciiString.utf8Bytes()
    }

    val indexOfScenarios = listOf(
        IndexOfScenario(
            "abc",
            'a',
            0
        ),
        IndexOfScenario(
            "abc",
            'z',
            -1
        )
    )

    @Test
    fun indexOfTest(){
        Assertions.assertThat(indexOfScenarios)
            .allSatisfy {
                val actual = it.bytes.indexOf({b -> b == it.target.code.toByte()})
                Assertions.assertThat(actual)
                    .isEqualTo(it.expectedIndex)
            }

    }

    data class ForEachWrappedRangeScenario(
        private val _bytes: String,
        val open: Byte,
        val close: Byte,
        val expectedRanges: List<IntRange>,
    ){
        val bytes get() = _bytes.utf8Bytes()
        companion object {
            fun of(
                asciiString: String,
                vararg expectedRanges: IntRange,
                open: Char = '[',
                close: Char = ']'
            ): ForEachWrappedRangeScenario {
                return ForEachWrappedRangeScenario(
                    asciiString,
                    open.code.toByte(),
                    close.code.toByte(),
                    expectedRanges.toList()
                )
            }

            fun ofSameDelimiter(
                asciiString: String,
                delimiter: Char,
                vararg expectedRanges: IntRange
            ): ForEachWrappedRangeScenario {
                return ForEachWrappedRangeScenario(
                    asciiString,
                    delimiter.code.toByte(),
                    delimiter.code.toByte(),
                    expectedRanges.toList()
                )
            }

            val scenarios = listOf(
                of(
                    "[]",
                    0..1
                ),
                of("["),
                of("]"),
                of("[["),
                of("]]"),
                of("[[]", 0..2),
                of("[][]", 0..1, 2..3),
                of("[][][", 0..1, 2..3),
                of("]["),
                ofSameDelimiter("||", '|', 0..1),
                ofSameDelimiter("|||", '|', 0..1),
                ofSameDelimiter("||||", '|', 0..1, 2..3)
            )
        }
    }

    @Test
    fun singleTest(){
        forEachWrappedRangeTest(ForEachWrappedRangeScenario.scenarios.first())
    }

    @Test
    fun forEachWrappedRange() {
        Assertions.assertThat(ForEachWrappedRangeScenario.scenarios)
            .allSatisfy {
                forEachWrappedRangeTest(it)
            }
    }

    private fun forEachWrappedRangeTest(scenario: ForEachWrappedRangeScenario) {
        val actualRanges = buildList {
            scenario.bytes.forEachWrappedRange(
                scenario.open,
                scenario.close,
            ) { start, endInclusive ->
                add(start..endInclusive)
            }
        }

        Assertions.assertThat(actualRanges)
            .containsExactlyElementsOf(scenario.expectedRanges)
    }
}