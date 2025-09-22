@file:OptIn(ExperimentalSerializationApi::class)

package brava.fightinwords.botlin.serialization

import androidx.collection.LongLongMap
import androidx.collection.buildLongLongMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection

object LongLongMapSerializer : KSerializer<LongLongMap> {
    override val descriptor: SerialDescriptor = LongArraySerializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: LongLongMap,
    ) {
        encoder.encodeCollection(
            descriptor,
            value.size * 2
        ) {
            var index = 0
            value.forEach { k, v ->
                encodeLongElement(
                    descriptor,
                    index,
                    k
                )

                index += 1

                encodeLongElement(
                    descriptor,
                    index,
                    v
                )

                index += 1
            }
        }
    }

    override fun deserialize(decoder: Decoder): LongLongMap {
        return decoder.decodeStructure(
            descriptor
        ) {
            val arraySize = decodeCollectionSize(descriptor)
            check(arraySize % 2 == 0) { "Size $arraySize wasn't an even number, so it can't have been a map!" }
            val mapSize = arraySize / 2
            // TODO: I'd rather build the `LongLongMap` directly from the two backing arrays, but it has some fancy internal `metadata` field that looks like it needs to be procedurally calculated (gross).
            return@decodeStructure buildLongLongMap(mapSize) {
                for (i in 0 until mapSize) {
                    val key = decodeLongElement(descriptor, i * 2)
                    val value = decodeLongElement(descriptor, i * 2 + 1)
                    put(key, value)
                }
            }
        }
    }
}