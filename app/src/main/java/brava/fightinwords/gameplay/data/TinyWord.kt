package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.lastIndex
import brava.fightinwords.gameplay.data.Word.Companion.indices
import org.jetbrains.annotations.ApiStatus
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * > # TL;DR:
 * > Serialize as [Word] or _(rarely)_ [packed].
 *
 * A [brava.fightinwords.gameplay.data.TinyWord] could be serialized in two ways:
 * - The [packed] value
 * - The [toString] representation
 *
 * Because they are roughly equivalent, the [toString] representation should be preferred, by explicitly casting to [Word].
 *
 * The one exception to this is:
 * - You are using a [kotlinx.serialization.BinaryFormat]
 * - You have a collection of exclusively [brava.fightinwords.gameplay.data.TinyWord]s
 *
 * In this scenario, you can avoid a lot of boxing by serializing the [packed] values as a specialized collection like [LongArray].
 */
@ApiStatus.Experimental
@JvmInline
value class TinyWord(val packed: Long) : Word {
    override val length: Int
        get() = (packed and 0b1111).toInt()

    private fun unpackLetter(index: Int) : Long {
        require(index in 0 until length) { "Index out of bounds: $index for $javaClass `$this`" }

        val charBits = (packed shr ((length - 1 - index) * 5 + 4)) and 0b11111
        return 'a'.code + charBits
    }

    override fun get(index: Int): TinyLetter {
        return TinyLetter.createUnsafe(unpackLetter(index).toByte())
    }

    inline fun forEach(action: (TinyLetter) -> Unit) {
        for (i in indices) {
            action(get(i))
        }
    }

    companion object {
        const val MAX_PACK = 12

        fun of(
            bytes: ByteSlice,
            start: Int = 0,
            endInclusive: Int = bytes.lastIndex,
        ) = TinyWord(
            packAZ(bytes::get, start, endInclusive)
        )

        fun of(
            byteBuffer: ByteBuffer,
            start: Int = 0,
            endInclusive: Int = byteBuffer.lastIndex,
        ) = TinyWord(
            packAZ(byteBuffer::get, start, endInclusive)
        )

        fun of(tinyLetters: Iterable<TinyLetter>): TinyWord {
            return when (tinyLetters) {
                is Collection<TinyLetter> -> fromCollection(tinyLetters)
                else -> {
                    var length = 0
                    var hash = 0L
                    for (l in tinyLetters) {
                        length += 1
                        length.requireLength()
                        hash = hash.packLetter(l.byteValue)
                    }
                    hash = hash.packLength(length)
                    return TinyWord(hash)
                }
            }
        }

        fun of(charSequence: CharSequence): TinyWord {
            return TinyWord(packAZ(charSequence));
        }

        fun CharSequence.toTinyWord() = of(this)


        internal inline fun extractTinyWordFromRange(
            wordDelimiter: Byte,
            start: Int,
            endInclusive: Int,
            getter: (index: Int) -> Byte,
        ): TinyWord {
            var hash = 0L
            var pos = start

            while (pos <= endInclusive) {
                val current = getter(pos)
                if (current == wordDelimiter) {
                    break
                } else {
                    hash = hash.packLetter(current)
                    pos += 1
                    require(pos - start <= MAX_PACK) {
                        "Reached the ${TinyWord::class.java} length limit of $MAX_PACK at the index $pos (byte: $current, char: ${
                            current.toInt().toChar()
                        }) without reaching either the delimiter (${
                            wordDelimiter.toInt().toChar()
                        }) OR the end of the range (at index $endInclusive, inclusive)"
                    }
                }
            }

            val length = pos - start
            hash = hash.packLength(length)
            return TinyWord(hash)
        }

        private fun fromCollection(letters: Collection<TinyLetter>): TinyWord {
            letters.size.requireLength()

            var hash = 0L
            for (l in letters) {
                hash = hash.packLetter(l.byteValue)
            }

            hash = hash.packLength(letters.size)
            return TinyWord(hash)
        }

        internal fun packAZ(s: CharSequence): Long {
            s.length.requireLength()
            var hash = 0L
            for (c in s) {
                hash = hash.packLetter(c)
            }
            return hash.packLength(s.length) // put length in last 4 bits
        }

        private fun Long.packLong(long: Long) = (this shl 5) or long

        private fun Long.packLowerAz(lowerAz: Byte) : Long = packLong((lowerAz - aByte).toLong())
        private fun Long.packLetter(letter: Char): Long = packLowerAz(letter.toLowerAz())
        private fun Long.packLetter(letter: Byte): Long = packLowerAz(letter.toLowerAz())
        private fun Long.packLength(length: Int): Long = (this shl 4) or length.toLong()

        private inline fun packAZ(
            getter: (Int) -> Byte,
            start: Int,
            endInclusive: Int,
        ): Long {
            val length = (endInclusive - start + 1).requireLength()
            var hash = 0L
            for (i in start..endInclusive) {
                val c = getter(i)
                hash = hash.packLetter(c)
            }

            return hash.packLength(length)
        }

        private fun packAZ(utf8Bytes: ByteBuffer): Long {
            var hash = 0L
            var length = 0
            while (utf8Bytes.hasRemaining()) {
                val c = utf8Bytes.get()
                hash = hash.packLetter(c)
                length += 1
                length.requireLength()
            }

            return hash.packLength(length)
        }

        private fun Int.requireLength(): Int {
            require(this in 1..MAX_PACK, { "Must be 1–$MAX_PACK characters of a–z" })
            return this
        }

        private const val aByte = 'a'.code.toByte()

        internal fun unpackAZ(hash: Long): String {
            val length = (hash and 0b1111).toInt() // last 4 bits = length
            require(length in 1..MAX_PACK)

            var bits = hash shr 4
            val chars = CharArray(length)

            for (i in (length - 1) downTo 0) {
                val value = (bits and 0b11111).toInt()
                chars[i] = ('a' + value)
                bits = bits shr 5
            }

            return String(chars)
        }

        internal fun writePackedWordFile(words: Sequence<String>, path: String) {
            DataOutputStream(BufferedOutputStream(FileOutputStream(path))).use { out ->
                for (word in words) {
                    val packed = packAZ(word)
                    out.writeLong(packed)
                }
            }
        }

        internal fun <T> readPackedWordFile(path: String, action: (Sequence<String>) -> T): T {
            DataInputStream(BufferedInputStream(FileInputStream(path))).use { input ->
                val words = iterator {
                    while (input.available() >= 8) { // 8 bytes = 64 bits
                        val packed = input.readLong()
                        val word = unpackAZ(packed)
                        yield(word)
                    }
                }

                return action(words.asSequence())
            }
        }

        internal fun Iterable<Letter>.tryGetTinyLetters(): TinyWord? {
            var hash = 0L
            var length = 0
            for (letter in this) {
                if (letter is TinyLetter) {
                    hash = hash.packLowerAz(letter.byteValue)
                    length += 1

                    if (length > MAX_PACK) {
                        return null
                    }
                }
            }

            hash = hash.packLength(length)
            return TinyWord(hash)
        }
    }
}