package brava.fightinwords.botlin

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharsetDecoder
import java.nio.charset.StandardCharsets

inline fun ByteBuffer.forEachLineRange(
    action: (start: Int, endInclusive: Int) -> Unit,
) {
    var lineStart = 0
    var pos = 0
    while (pos < limit()) {
        val current = get(pos)
        if (current == '\n'.code.toByte()) {
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

fun ByteBuffer.toUtf8String(): String = utf8().toString()

fun ByteSlice.toUtf8String(
    start: Int = 0,
    endInclusive: Int = lastIndex,
): String {
    if (endInclusive < start) {
        return ""
    }

    return toByteBuffer(start, endInclusive).toUtf8String()
}

private val threadLocalUtf8Decoder : ThreadLocal<CharsetDecoder> = ThreadLocal.withInitial { StandardCharsets.UTF_8.newDecoder() }

/**
 * @see kotlin.collections.lastIndex
 */
val ByteBuffer.lastIndex inline get() = limit() - 1

inline fun ByteBuffer.indexOf(
    predicate: (Byte) -> Boolean,
    startIndex: Int = 0,
    endInclusive: Int = lastIndex,
): Int {
    for (i in startIndex..endInclusive) {
        if (predicate(get(i))) {
            return i
        }
    }

    return -1
}

inline fun ByteSlice.indexOf(
    predicate: (Byte) -> Boolean,
    startIndex: Int = 0,
    endInclusive: Int = lastIndex,
): Int {
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

fun ByteSlice.indexOf(byte: Byte, startIndex: Int = 0, endInclusive: Int = lastIndex): Int {
    return indexOf({ it == byte }, startIndex, endInclusive)
}

inline fun ByteSlice.forEachWrappedRange(
    open: Byte,
    close: Byte,
    startIndex: Int = 0,
    action: (start: Int, endInclusive: Int) -> Unit,
) {
    var pos = startIndex
    while (pos <= lastIndex) {
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

val ByteBuffer.indices inline get() = position() until this.limit()

inline fun <reified T : Appendable> T.appendUtf8(
    byteSlice: ByteSlice,
    start: Int = 0,
    endInclusive: Int = byteSlice.lastIndex,
): T {
    if (endInclusive < start) {
        return this
    }

    val byteBuffer = byteSlice.toByteBuffer(start, endInclusive)
    append(byteBuffer.utf8())
    return this
}

fun String.utf8Bytes(
    start: Int = 0,
    endInclusive: Int = lastIndex,
): ByteBuffer = ByteBuffer.wrap(encodeToByteArray(start, endInclusive + 1, true))