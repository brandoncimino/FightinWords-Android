package brava.fightinwords.botlin.serialization

import androidx.collection.LongList
import androidx.collection.buildLongList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection

class LongListSerializer : KSerializer<LongList> {
    override val descriptor: SerialDescriptor = LongArraySerializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: LongList,
    ) {
        encoder.encodeCollection(
            descriptor,
            value.size
        ) {
            for (i in value.indices) {
                encodeLongElement(descriptor, i, value[i])
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): LongList {
        return decoder.decodeStructure(descriptor) {
            if (decodeSequentially()) {
                val collectionSize = decodeCollectionSize(descriptor)
                return@decodeStructure buildLongList(collectionSize) {
                    for (i in 0 until collectionSize) {
                        val element = decodeLongElement(descriptor, i)
                        add(element)
                    }
                }
            } else {
                return@decodeStructure buildLongList {
                    while (true) {
                        val elementIndex = decodeElementIndex(descriptor)
                        if (elementIndex == CompositeDecoder.DECODE_DONE) {
                            break
                        }

                        val element = decodeLongElement(descriptor, elementIndex)
                        add(element)
                    }
                }
            }
        }
    }
}