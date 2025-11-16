@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("UnstableApiUsage")

package brava.fightinwords.gameplay.data

import brava.fightinwords.Besting
import brava.fightinwords.Besting.assertRoundTrip
import brava.fightinwords.allSatisfy
import brava.fightinwords.assertEquality
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.TinyWord.Companion.toTinyWord
import brava.fightinwords.gameplay.data.Word.Companion.indices
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.Test

class WordTest {
    @Test
    fun subtypeEquality() {
        val stringWord = "¥Ø¬ø".toWord()
        println("stringWord = [${stringWord::class}]${stringWord}")
        val letterListWord = stringWord.indices.map { stringWord[it] }.toWord()
        println("letterListWord = [${letterListWord::class}]${letterListWord}")

        Assertions.assertThat(stringWord)
            .usingComparator(Word::lexicographicalCompare)
            .isEqualTo(stringWord)

        Besting.assertEquality(
            stringWord,
            letterListWord,
            recursiveEquality = false
        )

        val differentStringWord = "ß∑å©©ˆ˜ß".toWord()
        val differentLetterListWord =
            differentStringWord.indices.map { differentStringWord[it] }.toWord()
        val differentTinyWord = "yolo".toTinyWord()

        assertEquality(
            stringWord,
            letterListWord
        ) {
            byEquals()
            byString()
            byComparator(Word::lexicographicalCompare, "Word.lexicographicalCompare")
            byComparator(Word::shortlexCompare, "Word.shortlexCompare")
        }

        Besting.assertEquality(
            stringWord,
            differentStringWord,
            differentLetterListWord,
            differentTinyWord,
            expectedEquality = false,
            recursiveEquality = false
        )


        Besting.assertEquality(
            letterListWord,
            differentStringWord,
            differentLetterListWord,
            differentTinyWord,
            expectedEquality = false,
            recursiveEquality = false
        )
    }

    @Test
    fun roundTrip() {
        val words = listOf(
            "yolo".toWord(),
            "yølø".toWord(),
            "yolø".map { it.toLetter() }.toWord()
        )

        Assertions.assertThat(words)
            .allSatisfy(
                { it.assertRoundTrip(Cbor, recursiveEquality = false) },
                { it.assertRoundTrip(Json, recursiveEquality = false) }
            )
    }
}