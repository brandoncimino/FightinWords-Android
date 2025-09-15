package brava.fightinwords.botlin

import androidx.compose.ui.util.fastJoinToString
import brava.fightinwords.botlin.ListImplementation_forEachWrappedRangeTest.Wrappage.Unwrapped
import brava.fightinwords.botlin.ListImplementation_forEachWrappedRangeTest.Wrappage.Wrapped
import org.assertj.core.api.Assertions
import org.junit.Test

class ListImplementation_forEachWrappedRangeTest {
    enum class Wrappage {
        Unwrapped,
        Wrapped
    }

    @Test
    fun cSharpStyleInterpolatedString() {
        val inputString = "Hello {name}"

        val actualRanges = mutableListOf<Pair<String, Wrappage>>()

        ListImplementation.forEachWrappedRange(
            sourceStart = 0,
            sourceEndInclusive = inputString.lastIndex,
            isWrapperStart = { inputString[it] == '{' },
            isWrapperEndInclusive = { rangeStart, sourceIndex ->
                inputString[sourceIndex] == '}'
            },
            wrappedRangeAction = { start, endInclusive ->
                actualRanges.add(
                    inputString.substring(start..endInclusive) to Wrapped
                )
            },
            unwrappedRangeAction = { start, endInclusive ->
                actualRanges.add(
                    inputString.substring(start..endInclusive) to Unwrapped
                )
            }
        )

        Assertions.assertThat(actualRanges)
            .containsExactly(
                "Hello " to Unwrapped,
                "{name}" to Wrapped
            )
    }


    @Test
    fun quoteStyle_sameDelimiterStartAndEnd() {
        val inputString = "Use `Int` instead of `Char`"
        val expectedRanges = listOf(
            "Use " to Unwrapped,
            "`Int`" to Wrapped,
            " instead of " to Unwrapped,
            "`Char`" to Wrapped
        )

        val actualRanges = mutableListOf<Pair<String, Wrappage>>()

        ListImplementation.forEachWrappedRange(
            0,
            inputString.lastIndex,
            { inputString[it] == '`' },
            { rangeStart, sourceIndex ->
                inputString[sourceIndex] == '`'
            },
            wrappedRangeAction = { start, endInclusive ->
                actualRanges.add(inputString.substring(start..endInclusive) to Wrapped)
            },
            unwrappedRangeAction = { start, endInclusive ->
                actualRanges.add(inputString.substring(start..endInclusive) to Unwrapped)
            }
        )

        Assertions.assertThat(expectedRanges)
            .containsExactlyElementsOf(expectedRanges)
    }

    data class Data(
        val source: String,
        val description: String,
        val expectedRanges: List<Pair<String, Wrappage>>,
    ) {
        constructor(
            source: String,
            vararg expectedRanges: Pair<String, Wrappage>,
            description: String = "",
        ) : this(source, description = description, expectedRanges.toList())

        override fun toString(): String {

            return """
                    "$source" $description
                """.trimIndent()
        }
    }

    val scenarios = listOf(
        Data("", description = "Empty"),
        Data(
            "abc",
            "abc" to Unwrapped,
            description = "unwrapped-only",
        ),
        Data(
            "{abc}",
            "{abc}" to Wrapped,
            description = "wrapped only",
        ),
        Data(
            "{{}}",
            "{{}" to Wrapped,
            "}" to Unwrapped,
            description = "Nested",
        ),
        Data(
            "{{}{}}",
            "{{}" to Wrapped,
            "{}" to Wrapped,
            "}" to Unwrapped,
            description = "Nested 2",
        ),
        Data(
            "{{}}-{}}",
            "{{}" to Wrapped,
            "}-" to Unwrapped,
            "{}" to Wrapped,
            "}" to Unwrapped
        )
    )

    @Test
    fun forEachWrappedRange() {
        val expectedRanges: List<Pair<String, Wrappage>> = listOf(
            "--" to Unwrapped,
            "[abc]" to Wrapped,
            "--" to Unwrapped,
            "[[]" to Wrapped,
            "{[]}" to Wrapped
        )

        val input = expectedRanges.map { it.component1() }.fastJoinToString(separator = "")

        executeForEachWrappedRange(input, expectedRanges)
    }

    @Test
    fun simpleForEachWrappedRange() {
        executeForEachWrappedRange(
            input = "a[b]c",
            expectedRanges = listOf(
                "a" to Unwrapped,
                "[b]" to Wrapped,
                "c" to Unwrapped
            )
        )
    }

    @Test
    fun exhaustive() {
        Assertions.assertThat(scenarios)
            .allSatisfy { executeForEachWrappedRange(it.source, it.expectedRanges) }
    }

    private fun executeForEachWrappedRange(
        input: String,
        expectedRanges: List<Pair<String, Wrappage>>,
        description: String = "",
    ) {
        val actualRanges = mutableListOf<Pair<String, Wrappage>>()

        ListImplementation.forEachWrappedRange(
            sourceStart = 0,
            sourceEndInclusive = input.lastIndex,
            isWrapperStart = {
                val char = input[it]
                val isWrapperStart = char == '[' || char == '{'
                println("Checking for wrapper start: `$char` -> $isWrapperStart")
                isWrapperStart
            },
            isWrapperEndInclusive = { rangeStart, sourceIndex ->
                val startChar = input[rangeStart]
                val currentChar = input[sourceIndex]
                val isWrapperEnd = when (startChar) {
                    '[' -> currentChar == ']'
                    '{' -> currentChar == '}'
                    else -> false
                }

                println("Checking if `$currentChar` is the matching end for the wrapper start `$startChar`: $isWrapperEnd")
                isWrapperEnd
            },
            wrappedRangeAction = { start, endInclusive ->
                val range = start..endInclusive
                println("🎁 Wrapped: $range")
                actualRanges.add(input.slice(range) to Wrapped)
            },
            unwrappedRangeAction = { start, endInclusive ->
                val range = start..endInclusive
                println("🧖‍♀️ Unwrapped: $range -> `${input.substring(range)}`")
                actualRanges.add(input.slice(range) to Unwrapped)
            }
        )

        Assertions.assertThat(actualRanges)
            .describedAs(description)
            .containsExactlyElementsOf(expectedRanges)
    }
}