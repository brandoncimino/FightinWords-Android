package brava.fightinwords.gameplay.mixedbags

import brava.fightinwords.gameplay.WordStateFlavor
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word

class SerializableWordStates private constructor(
    @PublishedApi
    internal val flavorMap: IntToMixedLongObjectMultimap<Word>,
) {
    class Builder {
        private val flavorMapBuilder: IntToMixedLongObjectMultimap.Builder<Word> =
            IntToMixedLongObjectMultimap.Builder<Word>()

        fun addTinyWord(
            tinyWord: TinyWord,
            flavor: WordStateFlavor,
        ): Builder {
            flavorMapBuilder.putLong(flavor.id, tinyWord.packed)
            return this
        }

        fun addBigWord(
            bigWord: Word,
            flavor: WordStateFlavor,
        ): Builder {
            check(bigWord !is TinyWord) { "Passing a ${TinyWord::class.simpleName} to the generic ${Word::class.simpleName}-processing function will cause boxing, negating the already-questionable benefit of this silly class." }
            flavorMapBuilder.putObject(flavor.id, bigWord)
            return this
        }

        fun build(): SerializableWordStates {
            return SerializableWordStates(flavorMapBuilder)
        }
    }

    inline fun forEach(
        tinyWordAction: (TinyWord, WordStateFlavor) -> Unit,
        bigWordAction: (Word, WordStateFlavor) -> Unit,
    ) {
        flavorMap.forEach(
            { flavorId, packed ->
                tinyWordAction(
                    TinyWord(packed),
                    WordStateFlavor.fromId(flavorId)
                )
            },
            { flavorId, word ->
                bigWordAction(
                    word,
                    WordStateFlavor.fromId(flavorId)
                )
            }
        )
    }

    companion object {
        inline fun build(
            action: Builder.() -> Unit,
        ): SerializableWordStates {
            return Builder().apply(action).build()
        }
    }
}