package brava.fightinwords.gameplay.mixedbags

import androidx.collection.LongList
import androidx.collection.MutableLongList
import androidx.collection.mutableLongListOf
import brava.fightinwords.botlin.serialization.LongListSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus

/**
 * A silly attempt to reduce boxing allocations by segregating primitive [longs] from [objects]
 */
@ApiStatus.Experimental
@PublishedApi
@Serializable
internal sealed class LongObjectMixedBag<T>(
    @PublishedApi
    @Serializable(LongListSerializer::class)
    internal open val longs: LongList,
    @PublishedApi
    internal open val objects: List<T>,
) {
    class Builder<T>(
        override val longs: MutableLongList = mutableLongListOf(),
        override val objects: MutableList<T> = mutableListOf(),
    ) : LongObjectMixedBag<T>(longs, objects) {
        fun addLong(long: Long): Builder<T> {
            longs.add(long)
            return this
        }

        fun addObject(obj: T): Builder<T> {
            objects.add(obj)
            return this
        }

        fun build(): LongObjectMixedBag<T> {
            return this
        }
    }

    companion object {
        fun <T> build(action: Builder<T>.() -> Unit): LongObjectMixedBag<T> {
            return Builder<T>().apply(action).build()
        }
    }

    inline fun forEach(
        longAction: (Long) -> Unit,
        objectAction: (T) -> Unit,
    ) {
        longs.forEach(longAction)
        objects.forEach(objectAction)
    }
}