package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.LongLongMap
import androidx.collection.buildLongLongMap
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.TinyRange.Companion.length
import brava.fightinwords.botlin.forEachLineRange
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LongMappedLines(
    private val bytes: MappedByteBuffer,
    private val wordDefinitionRanges: LongLongMap,
) {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun getLine(key: Long): ByteBuffer? {
        val packedRange = wordDefinitionRanges.getOrDefault(key, TinyRange.empty.packed)
        val tinyRange = TinyRange(packedRange)
        return when {
            tinyRange.isEmpty -> null
            else              -> bytes.slice(
                tinyRange.start,
                tinyRange.length
            )
        }
    }

    companion object {
        inline fun create(
            file: File,
            keyExtractor: (ByteBuffer, Int, Int) -> Long,
        ): LongMappedLines {
            val mappedByteBuffer = file.getMemoryMappedBuffer()
            val wordDefinitionRanges = parseLineRanges(mappedByteBuffer, keyExtractor)
            return LongMappedLines(
                mappedByteBuffer,
                wordDefinitionRanges
            )
        }

        fun File.getMemoryMappedBuffer(): MappedByteBuffer {
            val randomAccessFile = RandomAccessFile(this, "r")
            val channel = randomAccessFile.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
        }

        inline fun parseLineRanges(
            buffer: MappedByteBuffer,
            keyExtractor: (ByteBuffer, start: Int, endInclusive: Int) -> Long,
        ): LongLongMap {
            return buildLongLongMap {
                buffer.forEachLineRange { start, endInclusive ->
                    val key = keyExtractor(buffer, start, endInclusive)
                    put(key, TinyRange.packInts(start, endInclusive))
                }
            }
        }


    }
}