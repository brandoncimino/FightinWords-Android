package brava.fightinwords.botlin.serialization

import androidx.collection.IntIntMap
import androidx.collection.buildIntIntMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection

object IntIntMapSerializer : KSerializer<IntIntMap> {
    override val descriptor: SerialDescriptor = LongArraySerializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: IntIntMap,
    ) {
        encoder.encodeCollection(
            descriptor,
            value.size * 2
        ) {
            var index = 0
            value.forEach { key, value ->
                encodeIntElement(descriptor, index, key)
                index += 1
                encodeIntElement(descriptor, index, value)
                index += 1
            }
        }
    }

    override fun deserialize(decoder: Decoder): IntIntMap {
        return decoder.decodeStructure(
            descriptor
        ) {
            val arraySize = decodeCollectionSize(descriptor)
            check(arraySize % 2 == 0) { "Size $arraySize wasn't an even number, so it can't have been a map!" }
            val mapSize = arraySize / 2
            // TODO: I'd rather build the `LongLongMap` directly from the two backing arrays, but it has some fancy internal `metadata` field that looks like it needs to be procedurally calculated (gross).
            return@decodeStructure buildIntIntMap(mapSize) {
                for (i in 0 until mapSize) {
                    val key = decodeIntElement(descriptor, i * 2)
                    val value = decodeIntElement(descriptor, i * 2 + 1)
                    put(key, value)
                }
            }
        }
    }
}