package brava.fightinwords.gameplay.data

import org.jetbrains.annotations.ApiStatus
import java.io.*
import java.nio.ByteBuffer

@ApiStatus.Experimental
@JvmInline
value class TinyWord(val packed: Long) : CharSequence {
    override val length: Int
        get() = (packed and 0b1111).toInt()

    override fun get(index: Int): Char {
        require(index in 0 until length) { "Index out of bounds: $index" }

        val charBits = (packed shr ((length - 1 - index) * 5 + 4)) and 0b11111
        return ('a' + charBits.toInt())
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        var hash = 0L
        for (i in startIndex until endIndex) {
            val c = this[i]
            hash = (hash shl 5) or (c - 'a').toLong()
        }

        hash = (hash shl 4) or (endIndex - startIndex).toLong()

        return TinyWord(hash)
    }

    companion object {
        const val MAX_PACK = 12

        fun packAZ(s: CharSequence): Long {
            s.length.requireLength()
            var hash = 0L
            for (c in s) {
                hash = hash.packLetter(c)
            }
            return hash.packLength(s.length) // put length in last 4 bits
        }

        private fun Long.packLong(long: Long) = (this shl 5) or long
        fun Long.packLetter(letter: Char): Long = packLong((letter.asLowerAZ() - 'a').toLong())
        fun Long.packLetter(letter: Byte): Long = packLong((letter.asLowerAZ() - aByte).toLong())
        fun Long.packLength(length: Int): Long = (this shl 4) or length.toLong()

        fun packAZ(utf8Bytes: ByteBuffer): Long {
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

        private fun Int.requireLength() {
            require(this in 1..MAX_PACK, { "Must be 1–$MAX_PACK characters of a–z" })
        }

        fun Char.asLowerAZ(): Char {
            return when (this) {
                in 'A'..'Z' -> lowercaseChar()
                in 'a'..'z' -> this
                else        -> throw IllegalArgumentException("Must be a case-insensitive a-z, not: $this")
            }
        }

        private const val aByte = 'a'.code.toByte()
        private const val zByte = 'z'.code.toByte()
        private const val AByte = 'A'.code.toByte()
        private const val ZByte = 'Z'.code.toByte()

        fun Byte.asLowerAZ(): Byte {
            return when (this) {
                in aByte..zByte -> this
                in AByte..ZByte -> (this + 32).toByte()
                else            -> throw IllegalArgumentException(
                    "Must be a case-insensitive a-z, not: ${
                        this.toInt().toChar()
                    }"
                )
            }
        }

        fun unpackAZ(hash: Long): String {
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

        fun writePackedWordFile(words: Sequence<String>, path: String) {
            DataOutputStream(BufferedOutputStream(FileOutputStream(path))).use { out ->
                for (word in words) {
                    val packed = packAZ(word)
                    out.writeLong(packed)
                }
            }
        }

        fun <T> readPackedWordFile(path: String, action: (Sequence<String>) -> T): T {
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

        /**
         * @throws IllegalArgumentException If any of the [Word.letters] isn't between `a`-`z`
         */
        fun Word.toTinyWord(): TinyWord {
            var hash = 0L

            for (letter in this) {
                hash = hash.packLetter(letter.character)
            }

            hash = hash.packLength(length)
            return TinyWord(hash)
        }
    }
}