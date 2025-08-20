package brava.fightinwords.botlin

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
}