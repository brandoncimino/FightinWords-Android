package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.AsciiBytes
import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.debugAssert
import brava.fightinwords.gameplay.data.TinyWord.Companion.MAX_PACK
import brava.fightinwords.gameplay.data.TinyWord.Companion.create
import brava.fightinwords.gameplay.data.Word.Companion.indices
import org.jetbrains.annotations.ApiStatus

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

    override fun toString(): String {
        return unpackAZ(packed) // TODO: the `unpackAZ` method isn't really necessary
    }

    companion object {
        const val MAX_PACK = 12

        @PublishedApi
        internal const val emptyPacked = (0L shl 4) or 0.toLong()

        @JvmStatic
        val empty inline get() = TinyWord(emptyPacked)

        fun of(
            asciiBytes: AsciiBytes,
            start: Int = 0,
            endInclusive: Int = asciiBytes.lastIndex,
        ) = of(asciiBytes.bytes, start, endInclusive)

        fun of(
            bytes: ByteSlice,
            start: Int = 0,
            endInclusive: Int = bytes.lastIndex,
        ) = create(
            { TinyLetter.create(bytes[it]) },
            start,
            endInclusive
        )

        fun CharSequence.asTinyWord(): TinyWord? {
            return tryCreate(
                { this[it].code },
                0,
                lastIndex
            )
        }

        fun CharSequence.toTinyWord(
            start: Int = 0,
            endInclusive: Int = lastIndex,
        ) = create(
            { TinyLetter.create(get(it)) },
            start,
            endInclusive
        )

        enum class LongWordHandling {
            Error,
            Skip
        }

        internal inline fun extractTinyWordFromRange(
            wordDelimiter: Byte,
            start: Int,
            endInclusive: Int,
            getter: (index: Int) -> Byte,
            longWordHandling: LongWordHandling = LongWordHandling.Error,
        ): TinyWord {
            var hash = 0L
            var pos = start

            while (pos <= endInclusive) {
                val current = getter(pos)
                if (current == wordDelimiter) {
                    break
                } else {
                    val tinyLetter = TinyLetter.create(current)
                    hash = hash.packLowerAz(tinyLetter.byteValue)
                    pos += 1
                    if (pos - start >= MAX_PACK) {
                        when (longWordHandling) {
                            LongWordHandling.Error -> throw rejectLongWord(
                                pos,
                                current,
                                wordDelimiter,
                                endInclusive
                            )

                            LongWordHandling.Skip  -> {
                                println("SKIPPING a word that's TOO LONG: pos $pos - start $start = ${pos - start}")
                                return empty
                            }
                        }
                    }
                }
            }

            val length = pos - start
            hash = hash.packLength(length)
            return TinyWord(hash)
        }

        internal fun rejectLongWord(
            pos: Int,
            current: Byte,
            wordDelimiter: Byte,
            endInclusive: Int,
        ): RuntimeException {
            return IllegalStateException(
                "Reached the ${TinyWord::class.java} length limit of $MAX_PACK at the index $pos (byte: $current, char: ${
                    current.toInt().toChar()
                }) without reaching either the delimiter (`${
                    wordDelimiter.toInt().toChar()
                }`) OR the end of the range (at index $endInclusive, inclusive)"
            )
        }

        @PublishedApi
        internal fun Long.packLowerAz(lowerAz: Byte): Long {
            debugAssert { lowerAz.toInt().toChar() in 'a'..'z' }
            // TODO: Why is this (which was previously called `packLong`) using `shl 5`, but `packLength` is using `shl 4`...?
            return (this shl 5) or (lowerAz - aByte).toLong()
        }

        @PublishedApi
        internal fun Long.packLength(length: Int): Long {
            debugAssert { length <= MAX_PACK }
            return (this shl 4) or length.toLong()
        }

        inline fun create(
            getter: (Int) -> TinyLetter,
            start: Int,
            endInclusive: Int,
        ): TinyWord {
            val length = (endInclusive - start + 1).requireLength()
            var hash = 0L
            for (i in start..endInclusive) {
                val tinyLetter = getter(i)
                hash = hash.packLowerAz(tinyLetter.byteValue)
            }

            return TinyWord(hash.packLength(length))
        }

        /**
         * Similar to [create], but returns `null` if we couldn't create a [TinyWord] for some reason, e.g.:
         * - The length would exceeded [MAX_PACK]
         * - The range contained non-[TinyLetter]s
         */
        inline fun tryCreate(
            getCodePointAtIndex: (index: Int) -> Int,
            start: Int,
            endInclusive: Int,
        ): TinyWord? {
            val length = (endInclusive - start + 1)
            if (length !in 0..MAX_PACK) {
                return null
            }

            var hash = 0L

            for (i in start..endInclusive) {
                val codePoint = getCodePointAtIndex(i)
                val asLowerAz = codePoint.asLowerAz()

                if (asLowerAz < 0) {
                    return null
                }

                hash = hash.packLowerAz(asLowerAz)
            }

            hash = hash.packLength(length)
            return TinyWord(hash)
        }

        @PublishedApi
        internal fun Int.requireLength(): Int {
            require(this in 0..MAX_PACK) {
                "The length $this is outside of the range ${0..MAX_PACK}"
            }
            return this
        }

        private const val aByte = 'a'.code.toByte()

        internal fun unpackAZ(hash: Long): String {
            val length = (hash and 0b1111).toInt() // last 4 bits = length
            length.requireLength()

            var bits = hash shr 4
            val chars = CharArray(length)

            for (i in (length - 1) downTo 0) {
                val value = (bits and 0b11111).toInt()
                chars[i] = ('a' + value)
                bits = bits shr 5
            }

            return String(chars)
        }
    }
}