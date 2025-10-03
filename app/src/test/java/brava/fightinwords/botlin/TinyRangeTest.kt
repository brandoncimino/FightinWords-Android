package brava.fightinwords.botlin

import brava.fightinwords.botlin.TinyRange.Companion.endInclusive
import brava.fightinwords.botlin.TinyRange.Companion.til
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.Test

class TinyRangeTest {
    @Test
    fun jsonTest() {
        val tinyRange = 1 til 4
        val tinyRangeJson = Json.encodeToString(tinyRange)
        println("tinyRange = $tinyRange")
        println("tinyRangeJson = $tinyRangeJson")
        val tinyRangeFromJson = Json.decodeFromString<TinyRange>(tinyRangeJson)
        println("tinyRangeFromJson = $tinyRangeFromJson")

        Assertions.assertThat(tinyRangeFromJson)
            .isEqualTo(tinyRange)
            .hasToString((1 until 4).toString())
    }

    @Test
    fun emptyTest() {
        val ranges = listOf(
            TinyRange.empty,
            TinyRange.startEndInclusive(1, 0),
            TinyRange.startEndInclusive(0, -1),
            TinyRange.startLength(0, 0),
            TinyRange.startLength(0, -1)
        )

        Assertions.assertThat(ranges)
            .allSatisfy { assertEmpty(it) }
    }

    private fun assertEmpty(actual: TinyRange) {
        Assertions.assertThat(actual)
            .describedAs { "${TinyRange::class.java.simpleName} $actual (packed into: ${actual.packed})" }
            .satisfies(
                {
                    Assertions.assertThat(it.length)
                        .describedAs { "length" }
                        .isEqualTo(0)
                },
                {
                    Assertions.assertThat(it.start)
                        .describedAs { "start" }
                        .isEqualTo(0)
                },
                {
                    Assertions.assertThat(it.endInclusive)
                        .describedAs { "endInclusive" }
                        .isEqualTo(-1)
                }
            )
    }
}