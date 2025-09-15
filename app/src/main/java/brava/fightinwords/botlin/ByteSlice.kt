package brava.fightinwords.botlin

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.botlin.TinyRange.Companion.length
import java.nio.ByteBuffer

/**
 * A *_read-only_* sub-section of a [ByteBuffer].
 *
 * Advantages of [brava.fightinwords.botlin.ByteSlice] over [ByteBuffer]:
 * - Read-only.
 * - Uses friendly, standard collection idioms like [length] instead of [ByteBuffer.limit].
 * - Avoids @[RequiresApi]&lpar;[Build.VERSION_CODES.UPSIDE_DOWN_CAKE]) by using [fastSlice] instead of [ByteBuffer.slice].
 */
data class ByteSlice(
    private val source: ByteBuffer,
    val range: TinyRange,
) {
    constructor(
        source: ByteBuffer,
        start: Int,
        endInclusive: Int,
    ) : this(source, TinyRange(start, endInclusive))

    val length inline get() = range.length

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun toByteBuffer(): ByteBuffer {
        return source.slice(range.start, range.length)
    }
}

fun ByteBuffer.fastSlice(start: Int, endInclusive: Int): ByteSlice {
    return ByteSlice(this, start, endInclusive)
}