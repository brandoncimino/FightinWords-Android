package brava.fightinwords.botlin

import brava.fightinwords.botlin.AsciiBytes.Companion.isAscii
import brava.fightinwords.botlin.AsciiBytes.Companion.toAscii
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.CharBuffer

class AsciiBytesTest {
    @Test
    fun isAsciiTest() {
        val strings = listOf(
            "♘˜˚øœˆ¨∆",
            "yolo",
            "",
            "\n"
        )

        Assertions.assertThat(strings)
            .allSatisfy {
                val bytes = it.utf8Bytes()

                Assertions.assertThat(bytes.isAscii())
                    .isEqualTo(this.runCatching { bytes.toAscii() }.isSuccess)
                    .isEqualTo(isAsciiEncodable(it))
            }
    }

    fun isAsciiEncodable(string: String): Boolean {
        return runCatching {
            Charsets.US_ASCII.newEncoder().encode(
                CharBuffer.wrap(string)
            )
        }.isSuccess
    }
}