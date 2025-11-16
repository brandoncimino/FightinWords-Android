package brava.fightinwords

import brava.fightinwords.Besting.assertRoundTrip
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.StringWord
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class SaveGameStateTest {
    @Test
    fun letterRoundTrip() {
        val letter = Letter.of('ß')

        Assertions.assertThat(listOf(Json, Cbor))
            .allSatisfy {
                letter.assertRoundTrip(it)
            }
    }

    @Test
    fun saveGameStateRoundTripTest() {
        val generatedStringWord = DataGenerator.basic.generate<StringWord>()
        Assertions.assertThat(generatedStringWord.length)
            .isGreaterThan(0)


        val generatedString = DataGenerator.basic.generate<String>()
        val generatedTinyWord = DataGenerator.basic.generate<TinyWord>()
        println("generatedString = ${generatedString}")
        println("generatedStringWord = ${generatedStringWord} // ${generatedStringWord.length}")
        println("generatedTinyWord = ${generatedTinyWord}")

        val generatedLetters = DataGenerator.basic.generate<List<Letter>>()
        println("generatedLetters = ${generatedLetters}")

        println("DataGenerator.basic.generate<Word>() = ${DataGenerator.basic.generate<Word>()}")

        val saveGameState = DataGenerator.basic.generate<SaveGameState>()

        saveGameState.gamePlan.letterPool
            .apply {
                println("letterPool = [${this::class}] $this")
            }

        saveGameState.gamePlan.assertRoundTrip(Cbor)
        saveGameState.ledgermanState.assertRoundTrip(Cbor)

        saveGameState.assertRoundTrip(Cbor)
        saveGameState.assertRoundTrip(Json)

        val serialFormat = Cbor

        val serialized = serialFormat.encodeToByteArray(saveGameState)
        val deserialized = serialFormat.decodeFromByteArray<SaveGameState>(serialized)

        Assertions.assertThat(deserialized)
            .usingRecursiveComparison()
            .withEqualsForType({ a, b -> a.toString() == b.toString() }, Word::class.java)
            .withComparatorForType<Word>(Word::compareTo, Word::class.java)
            .isEqualTo(saveGameState)
    }
}