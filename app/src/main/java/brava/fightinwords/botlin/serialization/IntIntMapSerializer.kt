package brava.fightinwords.botlin.serialization

import androidx.collection.IntIntMap
import androidx.collection.buildIntIntMap
import brava.fightinwords.botlin.blog
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): IntIntMap {
        return decoder.decodeStructure(
            descriptor
        ) {
            if (decodeSequentially()) {
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
            } else {
                this@IntIntMapSerializer.blog { "Using indefinite-length encoding (gross)." }
                buildIntIntMap {
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
                        val key = decodeIntElement(descriptor, keyIndex)

                        val valueIndex = decodeElementIndex(descriptor)
                        if (valueIndex == CompositeDecoder.DECODE_DONE) {
                            throw IllegalStateException("The key at index $keyIndex wasn't followed by a corresponding value!!")
                        }

                        val value = decodeIntElement(descriptor, valueIndex)

                        put(key, value)
                    }
                }
            }
        }
    }
}