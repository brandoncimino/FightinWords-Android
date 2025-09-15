package brava.fightinwords.botlin

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.botlin.TinyRange.Companion.length
import com.google.common.base.Preconditions
import java.nio.ByteBuffer

/**
 * A *_read-only_* sub-section of a [ByteBuffer].
 *
 * Advantages of [brava.fightinwords.botlin.ByteSlice] over [ByteBuffer]:
 * - Read-only.
 * - Uses friendly, standard collection idioms like [size] instead of [ByteBuffer.limit].
 * - Avoids @[RequiresApi]&lpar;[Build.VERSION_CODES.UPSIDE_DOWN_CAKE]) by using [fastSlice] instead of [ByteBuffer.slice].
 */
data class ByteSlice(
    private val source: ByteBuffer,
    val rangeInSource: TinyRange,
) {
    constructor(
        source: ByteBuffer,
        start: Int,
        endInclusive: Int,
    ) : this(source, TinyRange(start, endInclusive))

    val size inline get() = rangeInSource.length

    operator fun get(index: Int): Byte {
        return source[rangeInSource.start + index]
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun toByteBuffer(
        start: Int = 0,
        endInclusive: Int = lastIndex,
    ): ByteBuffer {
        return source.slice(
            rangeInSource.start + start,
            endInclusive - start + 1
        )
    }

    val lastIndex inline get() = size - 1
    val indices inline get() = 0..lastIndex

    fun slice(start: Int, endInclusive: Int): ByteSlice {
        if (endInclusive < start) {
            return empty
        }

        Preconditions.checkPositionIndexes(
            start,
            endInclusive + 1,
            size
        )

        return ByteSlice(
            source,
            rangeInSource.start + start,
            rangeInSource.start + endInclusive
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun slice(tinyRange: TinyRange): ByteSlice {
        return slice(tinyRange.start, tinyRange.endInclusive)
    }

    companion object {
        val empty: ByteSlice = ByteSlice(ByteBuffer.allocate(0), TinyRange.empty)
    }
}

fun ByteBuffer.fastSlice(start: Int, endInclusive: Int) = ByteSlice(this, start, endInclusive)
fun ByteBuffer.fastSlice(range: TinyRange) = ByteSlice(this, range)