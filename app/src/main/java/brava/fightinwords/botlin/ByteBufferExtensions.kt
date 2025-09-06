package brava.fightinwords.botlin

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.common.base.Ascii
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharsetDecoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal inline fun ByteBuffer.forEachLine(
    action: (ByteBuffer) -> Unit,
) {
    forEachLineRange { start, endInclusive ->
        val line = this.slice(start, (endInclusive - start) + 1)
        action(line)
    }
}

inline fun ByteBuffer.forEachLineRange(
    action: (start: Int, endInclusive: Int) -> Unit,
) {
    var lineStart = 0
    var pos = 0
    while (pos < limit()) {
        val current = get(pos)
        if (current == Ascii.LF) {
            action(lineStart, pos - 1)
            lineStart = pos + 1
        }

        pos += 1
    }

    action(lineStart, limit() - 1)
}

fun ByteBuffer.utf8() : CharBuffer {
    // TODO: Do this in a way that will:
    //   - Throw errors for non-UTF8 bytes, instead of `UTF_8.decode()`, which silently replaces them with garbage
    //   - Is thread-safe, unlike storing a configured `UTF_8.newDecoder()`
    //   - Won't cause unnecessary allocations, unlike calling `UTF_8.newDecoder()` every time
    //   - Works on `ByteBuffer`s, unlike `Utf8.isWellFormed()`
    //  The current implementation is based on `UTF_8.decode()`'s internal optimization.
    //  If I really wanted to waste my time on the tiniest optimization, I could maybe use a pool of decoders and rent them, but I mean...come on.
    return threadLocalUtf8Decoder.get()!!.decode(this)
}

fun ByteBuffer.toUft8String() : String = utf8().toString()

private val threadLocalUtf8Decoder : ThreadLocal<CharsetDecoder> = ThreadLocal.withInitial { StandardCharsets.UTF_8.newDecoder() }

/**
 * @see kotlin.collections.lastIndex
 */
val ByteBuffer.lastIndex get() = limit() - 1

inline fun ByteBuffer.indexOf(predicate: (Byte) -> Boolean, startIndex: Int = 0, endInclusive: Int = lastIndex) : Int {
    for(i in startIndex..endInclusive){
        if(predicate(get(i))){
            return i
        }
    }

    return -1
}

fun ByteBuffer.indexOf(byte: Byte, startIndex: Int = 0, endInclusive: Int = lastIndex) : Int {
    return indexOf({it == byte}, startIndex, endInclusive)
}

fun ByteBuffer.findWrappedRange(open: Byte, close: Byte, startIndex: Int = 0, endInclusive: Int = lastIndex): TinyRange {
    val startByteIndex = indexOf(open, startIndex, lastIndex)

    if(startByteIndex < 0){
        return TinyRange.empty
    }

    val end = indexOf(close, startByteIndex+1, lastIndex)

    if(end < 0){
        return TinyRange.empty
    }

    return TinyRange(startByteIndex, end)
}

fun ByteBuffer.forEachWrappedRange(
    open: Byte,
    close: Byte,
    startIndex: Int = 0,
    action: (start: Int, endInclusive: Int) -> Unit,
) {
    var pos = startIndex
    while(pos < limit()){
        val start = indexOf(open, pos)

        if(start < 0){
            return
        }

        val end = indexOf(close, start + 1)

        if(end < 0){
            return
        }

        action(start, end)
        pos = end+1
    }
}