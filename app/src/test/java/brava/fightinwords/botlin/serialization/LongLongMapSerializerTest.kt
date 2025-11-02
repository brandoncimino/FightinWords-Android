@file:OptIn(ExperimentalSerializationApi::class)

package brava.fightinwords.botlin.serialization

import androidx.collection.longLongMapOf
import brava.fightinwords.Besting.assertRoundTrip
import brava.fightinwords.Nicknamed.Companion.nicknamed
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import org.assertj.core.api.Assertions
import org.junit.Test

class LongLongMapSerializerTest {
    val defaultFormat = Cbor { useDefiniteLengthEncoding = true }

    @Test
    fun longLongMap_serializesAs_singleLongArray() {
        val map = longLongMapOf(1, 99, 2, 33)
        val flatLongs = buildList {
            map.forEach { key, value -> add(key); add(value) }
        }.toLongArray()

        val format = defaultFormat
        val serializedLongArray = format.encodeToByteArray(flatLongs)
        val serializedLongLongMap = format.encodeToByteArray(LongLongMapSerializer, map)

        Assertions.assertThat(serializedLongLongMap)
            .isEqualTo(serializedLongArray)
    }

    @Test
    fun longLongMapRoundTrip() {
        val map = longLongMapOf(1, 99, 2, 33)

        val formats = listOf(
            Cbor {}.nicknamed { "Default" },
            Cbor { useDefiniteLengthEncoding = true }.nicknamed { "useDefiniteLengthEncoding" }
        )

        Assertions.assertThat(formats)
            .allSatisfy { format ->
                map.assertRoundTrip(format.value, LongLongMapSerializer)
            }
    }
}