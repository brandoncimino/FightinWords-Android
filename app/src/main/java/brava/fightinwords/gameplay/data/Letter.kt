package brava.fightinwords.gameplay.data

import brava.fightinwords.gameplay.data.Letter.Companion.describe
import kotlinx.serialization.Serializable
import kotlin.ranges.contains

/**
 * A single glyph that we use for gameplay.
 *
 * This class exists to take the place of [Char] in most scenarios, but to support situations where a letter can't be represented by a single [Char], such as with [Char.lowercase].
 *
 * # Implementation Requirements:
 * A [Letter] is inherently *case-insensitive*.
 * The "canonical" form of a [Letter] should be *lower-case*.
 */
sealed interface Letter : Comparable<Letter> {
    companion object {
        fun Char.toLetter(): Letter = of(this)
        fun Char.describe(): String =
            "U+${this.code} `$this` ${Character.getName(this.code)} (${this.category}, ${this.isLetter()}, ${this.isWhitespace()})"

        fun of(char: Char): Letter {
            return when(val lower = char.asLowerAz()){
                notByte -> CodePointLetter(char.code)
                else -> TinyLetter.createUnsafe(lower)
            }
        }

        fun of(byte: Byte): Letter {
            return when(val lower = byte.asLowerAz()){
                notByte -> CodePointLetter(byte.toInt())
                else -> TinyLetter.createUnsafe(lower)
            }
        }

        fun of(codePoint: Int): Letter {
            return when(codePoint){
                in 'a'.code..'z'.code -> TinyLetter.createUnsafe(codePoint.toByte())
                else -> CodePointLetter(codePoint)
            }
        }

        /**
         * @throws IllegalArgumentException If my [codePoint] cannot be represented by a single [Char].
         * @see Character.isBmpCodePoint
         */
        fun Letter.toCharacterOrThrow(): Char {
            require(
                Character.isBmpCodePoint(codePoint),
                { "The letter `$this` cannot be represented by a single ${Char::class}!" })
            return codePoint.toChar()
        }
    }

    val codePoint: Int

    @Deprecated(
        "This is unsafe to use, because not all letters can be represented by a single character.",
        replaceWith = ReplaceWith("toCharacterOrThrow()")
    )
    val character: Char get() = toCharacterOrThrow()

    override fun compareTo(other: Letter): Int {
        // TODO: See if the actual `compareTo` implementation is ever used. If not, I should probably remove it and use a bespoke implementation that makes case-sensitivity explicit.
        return Character.toLowerCase(codePoint).compareTo(Character.toLowerCase(other.codePoint))
    }
}

/**
 * A [Letter] that is strictly limited to lowercase 'a' to 'z', which allows for certain optimizations such as [TinyWord].
 */
@Serializable
@JvmInline
value class TinyLetter private constructor(val byteValue: Byte) : Letter {
    constructor(character: Char) : this(character.code.toByte())

    init {
        assert(byteValue in lowerA..lowerZ, { "${byteValue.toInt().toChar().describe()} must be a lowercase letter between 'a' and 'z'." })
    }

    override val codePoint: Int
        get() = byteValue.toInt()

    @Suppress("OVERRIDE_DEPRECATION" /* A `TinyLetter` can safely be represented by a single `Char`. */)
    override val character: Char get() = byteValue.toInt().toChar()

    override fun toString(): String {
        return byteValue.toString()
    }

    fun compareTo(other: TinyLetter): Int {
        return byteValue.compareTo(other.byteValue)
    }

    companion object {
        private const val lowerA : Byte = 'a'.code.toByte()
        private const val lowerZ : Byte = 'z'.code.toByte()

        /**
         * Creates a new [brava.fightinwords.gameplay.data.TinyLetter] ***without validating [lowerAZ]***.
         */
        fun createUnsafe(lowerAZ: Byte) = TinyLetter(lowerAZ)

        /**
         * Creates a new [brava.fightinwords.gameplay.data.TinyLetter] containing the ***lowercase*** version of [azCaseInsensitive].
         *
         * @throws IllegalArgumentException If [azCaseInsensitive] isn't between `a..z` or `A..Z`.
         */
        fun create(azCaseInsensitive: Byte) = TinyLetter(azCaseInsensitive.asLowerAz().rejectNotAz(azCaseInsensitive))

        fun create(azCaseInsensitive: Int) = TinyLetter(azCaseInsensitive.asLowerAz().rejectNotAz(azCaseInsensitive))

        fun create(azCaseInsensitive: Char) = TinyLetter(azCaseInsensitive.asLowerAz().rejectNotAz(azCaseInsensitive))
    }
}

private const val a = 'a'.code.toByte()
private const val z = 'z'.code.toByte()
private const val A = 'A'.code.toByte()
private const val Z = 'Z'.code.toByte()
private const val notByte: Byte = (-1).toByte()

private fun Any.notAz() = IllegalArgumentException("Must be a lowercase 'a'..'z' or uppercase 'A'..'Z', not: $this")
private fun Byte.rejectNotAz(original: Any) : Byte = when(this){
    notByte -> throw original.notAz()
    else -> this
}

private fun Char.asLowerAz() : Byte = when(this) {
    in 'a'..'z' -> code.toByte()
    in 'A'..'Z' -> (code + 32).toByte()
    else -> -1
}

private fun Byte.asLowerAz() : Byte = when(this) {
    in a..z -> this
    in A..Z -> (this + 32).toByte()
    else -> -1
}

private fun Int.asLowerAz() : Byte = when(this){
    in 'a'.code..'z'.code -> this.toByte()
    in 'A'.code..'Z'.code -> (this + 32).toByte()
    else -> -1
}

internal fun Char.toLowerAz() : Byte = asLowerAz().rejectNotAz(this)
internal fun Byte.toLowerAz() : Byte = asLowerAz().rejectNotAz(this)
internal fun Int.toLowerAz(): Byte = asLowerAz().rejectNotAz(this)

@JvmInline
@Serializable
value class CodePointLetter internal constructor(override val codePoint: Int) : Letter