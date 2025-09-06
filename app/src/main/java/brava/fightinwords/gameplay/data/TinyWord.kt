package brava.fightinwords.gameplay.data

import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import org.jetbrains.annotations.ApiStatus
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

@ApiStatus.Experimental
@JvmInline
value class TinyWord(val packed: Long) : CharSequence {
    override val length: Int
        get() = (packed and 0b1111).toInt()

    private fun unpackLetter(index: Int) : Long {
        require(index in 0 until length) { "Index out of bounds: $index" }

        val charBits = (packed shr ((length - 1 - index) * 5 + 4)) and 0b11111
        return 'a'.code + charBits
    }

    override fun get(index: Int): Char {
        return unpackLetter(index).toInt().toChar()
    }

    fun getLetter(index: Int): TinyLetter {
        return TinyLetter.createUnsafe(unpackLetter(index).toByte())
    }

    fun contains(letter: Letter) : Boolean {
        return when(letter){
            is TinyLetter -> {
                for(i in 0..length){
                    if(getLetter(i) == letter){
                        return true
                    }
                }

                return false
            }
            else -> false
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): TinyWord {
        var hash = 0L
        for (i in startIndex until endIndex) {
            val c = this[i]
            hash = (hash shl 5) or (c - 'a').toLong()
        }

        hash = (hash shl 4) or (endIndex - startIndex).toLong()

        return TinyWord(hash)
    }

    fun toWord(): Word = Word(TinyWordLetters(this))

    companion object {
        const val MAX_PACK = 12

        fun of(byteBuffer: ByteBuffer) = TinyWord(packAZ(byteBuffer))

        fun packAZ(s: CharSequence): Long {
            s.length.requireLength()
            var hash = 0L
            for (c in s) {
                hash = hash.packLetter(c)
            }
            return hash.packLength(s.length) // put length in last 4 bits
        }

        private fun Long.packLong(long: Long) = (this shl 5) or long

        private fun Long.packLowerAz(lowerAz: Byte) : Long = packLong((lowerAz - aByte).toLong())
        fun Long.packLetter(letter: Char): Long = packLowerAz(letter.toLowerAz())
        fun Long.packLetter(letter: Byte): Long = packLowerAz(letter.toLowerAz())
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

        private const val aByte = 'a'.code.toByte()

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
                hash = hash.packLetter(TinyLetter.create(letter.codePoint).byteValue)
            }

            hash = hash.packLength(length)
            return TinyWord(hash)
        }
    }
}

@ApiStatus.Experimental
@JvmInline
value class TinyWordLetters(val word: TinyWord) : List<Letter> {
    override val size: Int
        get() = word.length

    override fun isEmpty(): Boolean {
        return word.isEmpty()
    }

    override fun contains(element: Letter): Boolean {
        return word.contains(element)
    }

    override fun iterator(): Iterator<Letter> {
        // TODO: Optimize with a dedicated `TinyWordLetterIterator` type
        return iterator {
            for (i in 0..word.length) {
                yield(word.getLetter(i))
            }
        }
    }

    override fun containsAll(elements: Collection<Letter>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): Letter {
        return word.get(index).toLetter()
    }

    override fun indexOf(element: Letter): Int {
        for(i in indices){
            if(word.getLetter(i) == element){
                return i
            }
        }

        return -1
    }

    override fun lastIndexOf(element: Letter): Int {
        for(i in indices.reversed()){
            if(word.getLetter(i) == element){
                return i
            }
        }

        return -1
    }

    override fun listIterator(): ListIterator<Letter> {
        return listIterator(0)
    }

    override fun listIterator(index: Int): ListIterator<Letter> {
        return object : AbstractList<Letter>(){
            override val size: Int
                get() = word.length

            override fun get(index: Int): Letter {
                return word.getLetter(index)
            }
        }.listIterator(index)
    }

    override fun subList(
        fromIndex: Int,
        toIndex: Int,
    ): TinyWordLetters {
        return TinyWordLetters(word.subSequence(fromIndex, toIndex))
    }

}