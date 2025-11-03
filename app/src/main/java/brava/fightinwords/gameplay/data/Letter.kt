package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.debugAssert
import kotlinx.serialization.Serializable

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
        fun Int.toLetter(): Letter = of(this)

        fun Letter.describe(): String = "U+${this.codePoint} `$this` ${Character.getName(this.codePoint) ?: "unassigned"} (${this.category})"

        /**
         * @see Char.category
         */
        val Letter.category : CharCategory get() = CharCategory.valueOf(Character.getType(codePoint))

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

    /**
     * The [Character.charCount] of my [codePoint].
     */
    val lengthInChars: Int get() = Character.charCount(codePoint)

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
@Serializable // TODO: probably remove this `@Serializable`
@JvmInline
value class TinyLetter private constructor(
    val byteValue: Byte,
) : Letter {
    init {
        debugAssert { byteValue in lowerA..lowerZ }
    }

    override val codePoint: Int
        inline get() = byteValue.toInt()

    override val lengthInChars: Int
        inline get() = 1

    @Suppress("OVERRIDE_DEPRECATION" /* A `TinyLetter` can safely be represented by a single `Char`. */)
    override val character: Char inline get() = byteValue.toInt().toChar()

    override fun toString(): String {
        return byteValue.toInt().toChar().toString()
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

private fun Any.notAz() = IllegalArgumentException(
    "Must be a lowercase 'a'..'z' or uppercase 'A'..'Z', not: ${
        when (this) {
            is Int -> describeCodePoint(this)
            is Char -> describeCodePoint(this.code)
            is Byte -> describeCodePoint(this.toInt())
            else -> this
        }
    }"
)
private fun Byte.rejectNotAz(original: Any) : Byte = when(this){
    notByte -> throw original.notAz()
    else -> this
}

@PublishedApi
internal fun Char.asLowerAz(): Byte = when (this) {
    in 'a'..'z' -> code.toByte()
    in 'A'..'Z' -> (code + 32).toByte()
    else -> -1
}

internal fun Byte.asLowerAz(): Byte = when (this) {
    in a..z -> this
    in A..Z -> (this + 32).toByte()
    else -> -1
}

@PublishedApi
internal fun Int.asLowerAz(): Byte = when (this) {
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

private fun describeCodePoint(codePoint: Int): String =
    when (Character.isValidCodePoint(codePoint)) {
        true  -> "U+${codePoint} `${Character.toString(codePoint)}` ${Character.getName(codePoint) ?: "unassigned"} (${
            getCategory(codePoint)
        })"

        false -> "INVALID Unicode point: $codePoint"
    }

private fun getCategory(codePoint: Int): CharCategory =
    CharCategory.valueOf(Character.getType(codePoint))