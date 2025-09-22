@file:OptIn(ExperimentalSerializationApi::class)

package brava.fightinwords.botlin.serialization

import androidx.collection.LongLongMap
import androidx.collection.buildLongLongMap
import brava.fightinwords.botlin.blog
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection

object LongLongMapSerializer : KSerializer<LongLongMap> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<Long>()

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
                encodeLongElement(descriptor, index, k)
                index += 1

                encodeLongElement(descriptor, index, v)
                index += 1
            }
        }
    }

    override fun deserialize(decoder: Decoder): LongLongMap {
        return decoder.decodeStructure(
            descriptor
        ) {
            if (decodeSequentially()) {
                // TODO: No matter what I do, I cannot get this to trigger. Not with my own serializers, and not with built-in ones.

                val arraySize = decodeCollectionSize(descriptor)
                check(arraySize % 2 == 0) { "Size $arraySize wasn't an even number, so it can't have been a map!" }
                val mapSize = arraySize / 2
                return@decodeStructure buildLongLongMap(mapSize) {
                    for (i in 0 until mapSize) {
                        val key = decodeLongElement(descriptor, i * 2)
                        val value = decodeLongElement(descriptor, i * 2 + 1)
                        put(key, value)
                    }
                }
            } else {
                this@LongLongMapSerializer.blog { "Using indefinite-length encoding (gross)." }
                buildLongLongMap {
                    while (true) {
                        // Theoretically, this weird `decodeElementIndex` flow supports "out-of-order" collections.
                        // Supposedly, instead of getting data that contains:
                        //    [a, b, c]
                        // You might get:
                        //    [(1, b), (2, c), (0, a)]
                        // 📎 Whether it's the input that's out-of-order or the parser, I don't know. My guess is it would have to be the data, right?

                        // TODO: Question - which method actually advances the state of the decoder, then; `decode{x}Element`, or `decodeElementIndex`?
                        // TODO: Question - is this library actually any good? It seems real janky. I suppose the code generation is nice.
                        //       For example:
                        //          - Why do you need to pass the `descriptor` around everywhere, if you're supposed to _always_ pass the same one?
                        //          - Why can't you serializer a `CharSequence`? Why are you forced to turn it into a `String` first?
                        val keyIndex = decodeElementIndex(descriptor)

                        if (keyIndex == CompositeDecoder.DECODE_DONE) {
                            break
                        }

                        // 📎 In Cbor, at least, the index passed to `decode{x}Element` is completely irrelevant.
                        //    You can literally pass `Random.nextInt()` and everything will work fine.
                        //    The result of `decodeElementIndex` is used only to check if the collection is finished.
                        val key = decodeLongElement(descriptor, keyIndex)

                        val valueIndex = decodeElementIndex(descriptor)
                        if (valueIndex == CompositeDecoder.DECODE_DONE) {
                            throw IllegalStateException("The key at index $keyIndex wasn't followed by a corresponding value!!")
                        }

                        val value = decodeLongElement(descriptor, valueIndex)

                        put(key, value)
                    }
                }
            }
        }
    }
}