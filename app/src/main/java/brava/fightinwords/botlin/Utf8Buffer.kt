package brava.fightinwords.botlin

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.botlin.TinyRange.Companion.length
import com.google.common.collect.Iterables
import org.jetbrains.annotations.ApiStatus
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

@ApiStatus.Experimental
sealed interface Utf8Buffer<SELF : Utf8Buffer<SELF>> {
    fun slice(start: Int, length: Int) : SELF

    fun decode() : Utf8CharBuffer

    companion object {
        operator fun <SELF : Utf8Buffer<SELF>> Utf8Buffer<SELF>.get(tinyRange: TinyRange) : SELF {
            return slice(tinyRange.start, tinyRange.length)
        }
    }
}

data class Utf8ByteBuffer(val byteBuffer: ByteBuffer) : Utf8Buffer<Utf8ByteBuffer> {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun slice(start: Int, length: Int): Utf8ByteBuffer {
        return Utf8ByteBuffer(byteBuffer.slice(start, length))
    }

    override fun decode(): Utf8CharBuffer {
        return Utf8CharBuffer(StandardCharsets.UTF_8.decode(byteBuffer))
    }
}

data class Utf8CharBuffer(val charBuffer: CharBuffer) : Utf8Buffer<Utf8CharBuffer> {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun slice(start: Int, length: Int): Utf8CharBuffer {
        return Utf8CharBuffer(charBuffer.slice(start, length))
    }

    override fun decode(): Utf8CharBuffer = this
}

