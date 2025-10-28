package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.toUtf8String
import brava.fightinwords.botlin.utf8
import brava.fightinwords.botlin.utf8Bytes
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


class NaspaWordListEntryTest {
    data class Data(
        val rawString: String,
        val word: String,
        val definition: String,
        val partOfSpeech: String,
        val relatedWords: List<String> = listOf(),
    ) {
        val wordRange get() = rawString.rangeOf(word)
        val definitionRange get() = rawString.rangeOf(definition)
        val partOfSpeechRange get() = rawString.rangeOf(partOfSpeech)
    }

    companion object {
        val Sundae = Data(
            "SUNDAE a dish of ice cream served with a topping [n SUNDAES]",
            "SUNDAE",
            "a dish of ice cream served with a topping",
            "n",
            listOf("SUNDAES")
        )

        val Sundaes = Data(
            "SUNDAES <sundae=n> [n]",
            "SUNDAES",
            "<sundae=n>",
            "n"
        )

        val Summon = Data(
            "SUMMON to order to appear [v SUMMONED, SUMMONING, SUMMONS] : SUMMONABLE [adj], SUMMONER [n]",
            "SUMMON",
            "to order to appear",
            "v",
            listOf("SUMMONED, SUMMONING, SUMMONS")
        )

        val Summons = Data(
            "SUMMONS <summon=v> [v] / to summon with a court order [v SUMMONSED, SUMMONSES, SUMMONSING]",
            "SUMMONS",
            "<summon=v>",
            "v",
        )

        val Summoned = Data(
            "SUMMONED <summon=v> [v]",
            "SUMMONED",
            "<summon=v>",
            "v"
        )

        val Summoner = Data(
            "SUMMONER one that {summons=v} [n SUMMONERS]",
            "SUMMONER",
            "one that {summons=v}",
            "n",
            listOf("SUMMONERS")
        )

        val Summonable = Data(
            "SUMMONABLE capable of being {summoned=v} [adj]",
            "SUMMONABLE",
            "capable of being {summoned=v}",
            "adj"
        )

        fun ByteBuffer.toUtf8String(): String {
            val charBuffer = StandardCharsets.UTF_8.decode(this.slice())
            return charBuffer.toString()
        }

        fun String.rangeOf(substring: String): TinyRange {
            val index = indexOf(substring)
            if (index < 0) {
                return TinyRange.empty
            }

            return TinyRange.startLength(index, substring.length)
        }

        val NaspaWordListEntry.wordString get() = rawEntry.toUtf8String(wordRange)
        val NaspaWordListEntry.partOfSpeechString get() = rawEntry.toUtf8String(partOfSpeechRange)
        val NaspaWordListEntry.definitionString get() = rawEntry.toUtf8String(definitionRange)
    }

    @Test
    fun parseTest() {
        Assertions.assertThat(
            listOf(
                Sundae,
                Sundaes,
                Summon,
                Summonable,
                Summons,
                Summoned,
                Summoner,
            )
        ).allSatisfy { parseTest(it) }
    }

    @Test
    fun parseTestSingle() {
        parseTest(Sundae)
    }

    @Test
    fun toUtf8StringTest() {
        val fullString = "yolo/swag"
        val slashIndex = fullString.indexOf('/')

        val byteBuffer = ByteBuffer.wrap(fullString.encodeToByteArray())

        Assertions.assertThat(byteBuffer.limit())
            .isEqualTo(fullString.length)

        val byteBufferSlice = byteBuffer.slice(0, slashIndex)
        val byteBufferSliceString = byteBufferSlice.toUtf8String()
        Assertions.assertThat(byteBufferSliceString)
            .isEqualTo("yolo")

        val slice = fullString.utf8Bytes(0, slashIndex - 1)
        val actualBytes = slice.toByteBuffer()
        val actualString = actualBytes.utf8().toString()

        Assertions.assertThat(actualString)
            .isEqualTo(byteBufferSliceString)
    }

    fun ByteBuffer.toList(): List<Byte> {
        val start = position()
        val limit = limit()
        val bytes = this
        return buildList {
            for (i in start until (start + limit)) {
                add(bytes.get(i))
            }
        }
    }

    fun parseTest(data: Data) {
        val parsed = NaspaWordListEntry.parse(data.rawString.utf8Bytes())

        println("parsed.wordRange = ${parsed.wordRange}")
        println("data.wordRange = ${data.wordRange}")

        Assertions.assertThat(parsed)
            .satisfies(
                {
                    Assertions.assertThat(it.wordString)
                        .describedAs { "word" }
                        .isEqualToIgnoringCase(data.word)
                },
                {
                    Assertions.assertThat(it.partOfSpeechString)
                        .describedAs { "partOfSpeech" }
                        .isEqualToIgnoringCase(data.partOfSpeech)
                },
                {
                    Assertions.assertThat(it.definitionString)
                        .describedAs { "definition" }
                        .isEqualToIgnoringCase(data.definition)
                }
            )
    }

    @Test
    fun jsonTest() {

    }
}