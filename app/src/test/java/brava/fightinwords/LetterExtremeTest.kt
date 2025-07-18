package brava.fightinwords

import org.assertj.core.api.Assertions
import org.junit.Test
import java.text.Normalizer


class LetterExtremeTest {
    @Test
    fun getPlatonic() {
        val scenarios = mapOf(
            "A" to "a",
            "a" to "a",
            "Ǎ" to "a",
            "ĳ" to "ĳ",
            "Ĳ" to "ĳ",
        )
            .flatMap { (raw, platonic) ->
                mapOf(
                    Normalizer.normalize(raw, Normalizer.Form.NFD) to platonic,
                    Normalizer.normalize(raw, Normalizer.Form.NFC) to platonic
                ).entries
            }

        Assertions.assertThat(scenarios)
            .allSatisfy { kvp ->
                val letterExtreme = LetterExtreme(kvp.key)
                Assertions.assertThat(letterExtreme.platonic)
                    .describedAs("platonic")
                    .isEqualTo(kvp.value);
            }
    }

    @Test
    fun sortPhonologically() {
        val expectedSorted = listOf(
            "A",
            "a",
            "y",
            "D",
            "Đ"
        )
            .map { LetterExtreme(it) }

        Assertions.assertThat(expectedSorted.shuffled().sortedWith(LetterExtreme.phonologicalComparator))
            .isEqualTo(expectedSorted)
    }
}


