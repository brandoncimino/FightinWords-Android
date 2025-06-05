package brava.fightinwords

import org.assertj.core.api.Assertions
import org.junit.Test
import java.text.Normalizer


class LetterTest {
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
                val letter = Letter(kvp.key)
                Assertions.assertThat(letter.platonic)
                    .describedAs("platonic")
                    .isEqualTo(kvp.value);
            }
    }
}


