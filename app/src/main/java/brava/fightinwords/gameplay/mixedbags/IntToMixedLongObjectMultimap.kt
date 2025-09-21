package brava.fightinwords.gameplay.mixedbags

import androidx.collection.IntObjectMap
import androidx.collection.MutableIntObjectMap
import androidx.collection.mutableIntObjectMapOf

/**
 * ...A [com.google.common.collect.Multimap]-style collection that maps [Int]s to [LongObjectMixedBag]s
 */
@PublishedApi
internal sealed class IntToMixedLongObjectMultimap<T>(
    @PublishedApi
    internal open val bags: IntObjectMap<out LongObjectMixedBag<T>>,
) {
    class Builder<T>(
        override val bags: MutableIntObjectMap<LongObjectMixedBag.Builder<T>> = mutableIntObjectMapOf(),
    ) : IntToMixedLongObjectMultimap<T>(bags) {

        private fun getBag(bagKey: Int) = bags.getOrPut(bagKey, { LongObjectMixedBag.Builder() })

        fun putLong(bagKey: Int, longValue: Long): Builder<T> {
            getBag(bagKey).addLong(longValue)
            return this
        }

        fun putObject(bagKey: Int, obj: T): Builder<T> {
            getBag(bagKey).addObject(obj)
            return this
        }

        fun build() = this
    }

    companion object {
        inline fun <T> build(action: Builder<T>.() -> Unit): IntToMixedLongObjectMultimap<T> {
            return Builder<T>().apply(action).build()
        }
    }

    inline fun forEach(
        longAction: (key: Int, Long) -> Unit,
        objectAction: (key: Int, T) -> Unit,
    ) {
        bags.forEach { key, bag ->
            bag.forEach(
                { longAction(key, it) },
                { objectAction(key, it) }
            )
        }
    }
}