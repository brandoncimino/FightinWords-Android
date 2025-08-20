package brava.fightinwords.gameplay.wordlookup

import org.assertj.core.api.Assertions
import org.junit.Test


class NaspaWordListEntryTest {
    data class Data(
        val raw: String,
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
        val parsed = NaspaWordListEntry.parse(data.raw)

        Assertions.assertThat(parsed)
            .satisfies(
                {
                    Assertions.assertThat(it.word)
                        .describedAs { "word" }
                        .isEqualToIgnoringCase(data.word)
                },
                {
                    Assertions.assertThat(it.partOfSpeech)
                        .describedAs { "partOfSpeech" }
                        .isEqualToIgnoringCase(data.partOfSpeech)
                },
                {
                    Assertions.assertThat(it.definition)
                        .describedAs { "definition" }
                        .isEqualToIgnoringCase(data.definition)
                }
            )
    }

    @Test
    fun jsonTest() {

    }
}