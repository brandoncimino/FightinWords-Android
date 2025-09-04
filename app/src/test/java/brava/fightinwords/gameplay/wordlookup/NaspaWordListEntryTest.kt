package brava.fightinwords.gameplay.wordlookup

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
    )

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
            val charBuffer = StandardCharsets.UTF_8.decode(this)
            return charBuffer.toString()
        }

        fun String.utf8Bytes(): ByteBuffer {
            return ByteBuffer.wrap(this.encodeToByteArray())
        }
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

    fun parseTest(data: Data) {
        val parsed = NaspaWordListEntry.parse(data.rawString.utf8Bytes())

        Assertions.assertThat(parsed)
            .satisfies(
                {
                    Assertions.assertThat(it.word.toUtf8String())
                        .describedAs { "word" }
                        .isEqualToIgnoringCase(data.word)
                },
                {
                    Assertions.assertThat(it.partOfSpeech.toUtf8String())
                        .describedAs { "partOfSpeech" }
                        .isEqualToIgnoringCase(data.partOfSpeech)
                },
                {
                    Assertions.assertThat(it.definition.toUtf8String())
                        .describedAs { "definition" }
                        .isEqualToIgnoringCase(data.definition)
                }
            )
    }

    @Test
    fun jsonTest() {

    }
}