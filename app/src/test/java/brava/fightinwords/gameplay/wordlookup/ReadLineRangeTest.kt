package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.Nicknamed.Companion.nicknamed
import brava.fightinwords.botlin.fastSlice
import brava.fightinwords.botlin.forEachLineRange
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class ReadLineRangeTest {
    fun String.byteBuffer(): ByteBuffer = ByteBuffer.wrap(this.toByteArray(StandardCharsets.UTF_8))

    val multilineStrings = listOf(
        "".nicknamed { "empty string (1 empty line)" },
        "a".nicknamed { "one 'a', no breaks" },
        "\n".nicknamed { "one line break" },
        "\na".nicknamed { "break-'a'" }
    )

    @Test
    fun forEachLineRangeTest() {
        Assertions.assertThat(multilineStrings)
            .allSatisfy { str ->
                val lineRanges = buildList {
                    str.value.byteBuffer().fastSlice().forEachLineRange { start, endInclusive ->
                        println("Range: $start..$endInclusive")
                        add(start..endInclusive)
                    }
                }

                val lineStrings = lineRanges.map { str.value.substring(it) }
                println("lineStrings = ${lineStrings}")

                Assertions.assertThat(lineStrings)
                    .describedAs { "Matches kotlin lines" }
                    .isEqualTo(str.value.lines())
                    .allSatisfy { Assertions.assertThat(it).doesNotContain("\n") }
            }
    }
}