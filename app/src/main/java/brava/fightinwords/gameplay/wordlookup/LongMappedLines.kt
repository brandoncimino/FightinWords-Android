package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.collection.LongLongMap
import androidx.collection.buildLongLongMap
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.fastSlice
import brava.fightinwords.botlin.forEachLineRange
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LongMappedLines(
    private val bytes: ByteBuffer,
    internal val wordDefinitionRanges: LongLongMap,
) {
    fun findLine(key: Long): ByteSlice? {
        val packedRange = wordDefinitionRanges.getOrDefault(key, TinyRange.empty.packed)
        val tinyRange = TinyRange(packedRange)
        return when {
            tinyRange.isEmpty -> null
            else -> bytes.fastSlice(tinyRange)
        }
    }

    internal inline fun <reified T> useLine(
        key: Long,
        action: (start: Int, endInclusive: Int) -> T,
    ): T? {
        val packedRange = wordDefinitionRanges.getOrDefault(key, TinyRange.empty.packed)
        val tinyRange = TinyRange(packedRange)
        return when {
            tinyRange.isEmpty -> null
            else              -> action(tinyRange.start, tinyRange.endInclusive)
        }
    }

    /**
     * Checks for the presence of [key] in my [wordDefinitionRanges].
     * Analogous to [Map.containsKey].
     *
     * While the performance benefit of this over [findLine] is probably insignificant, this method has the benefit of not requiring [Build.VERSION_CODES.UPSIDE_DOWN_CAKE].
     */
    fun containsKey(key: Long) : Boolean = wordDefinitionRanges.containsKey(key)

    companion object {
        inline fun create(
            file: File,
            keyExtractor: (ByteBuffer, Int, Int) -> Long,
        ): LongMappedLines {
            val mappedByteBuffer = file.getMemoryMappedBuffer()
            return create(mappedByteBuffer, keyExtractor)
        }

        inline fun create(
            bytes: ByteBuffer,
            keyExtractor: (ByteBuffer, Int, Int) -> Long,
        ): LongMappedLines {
            val wordDefinitionRanges = parseLineRanges(bytes, keyExtractor)
            return LongMappedLines(
                bytes,
                wordDefinitionRanges
            )
        }

        fun File.getMemoryMappedBuffer(): MappedByteBuffer {
            val randomAccessFile = RandomAccessFile(this, "r")
            val channel = randomAccessFile.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
        }

        inline fun parseLineRanges(
            bytes: ByteBuffer,
            keyExtractor: (ByteBuffer, start: Int, endInclusive: Int) -> Long,
        ): LongLongMap {
            return buildLongLongMap {
                bytes.forEachLineRange { start, endInclusive ->
                    val key = keyExtractor(bytes, start, endInclusive)
                    put(key, TinyRange.packInts(start, endInclusive))
                }
            }
        }


    }
}