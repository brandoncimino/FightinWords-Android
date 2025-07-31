package brava.fightinwords.botlin

import brava.fightinwords.botlin.TinyFlags.Companion.MAX_FLAG
import brava.fightinwords.botlin.TinyFlags.Companion.MIN_FLAG
import brava.fightinwords.botlin.TinyFlags.Companion.enable
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Contract

/**
 * A super-efficient way to represent a map of [Boolean]s to the numbers [0][MIN_FLAG] to [31][MAX_FLAG] (inclusive) as a single [Int].
 *
 * Functionally similar to C#'s [[[Flags]]](https://learn.microsoft.com/en-us/dotnet/fundamentals/runtime-libraries/system-flagsattribute), as well as the internal representation of Java's [java.util.EnumSet].
 */
@JvmInline
@Serializable
value class TinyFlags private constructor(val bitFlags: Int) : Set<Int> {
    constructor() : this(0)

    @Contract(pure = true)
    fun hasFlag(flag: Int): Boolean = bitFlags and (1 shl flag.validate()) != 0

    operator fun plus(addend: TinyFlags) = TinyFlags(bitFlags or addend.bitFlags)
    operator fun plus(flag: Int) = enable(flag)
    operator fun minus(subtrahend: TinyFlags) = TinyFlags(bitFlags and subtrahend.bitFlags.inv())
    operator fun minus(flag: Int) = disable(flag)

    @Contract(pure = true)
    operator fun get(flag: Int): Boolean = hasFlag(flag)

    /**
     * Either [enable]s or [disable]s [flag].
     *
     * 📎 Note: This cannot use the `operator` keyword because Kotlin assumes that the `set` operator operates via side-effects.
     * In other words, this syntax is illegal:
     * ```kotlin
     * val updated = original[1] = true
     * ```
     */
    @Contract(pure = true)
    fun set(flag: Int, enabled: Boolean): TinyFlags = when {
        enabled -> this@TinyFlags.enable(flag)
        else    -> disable(flag)
    }

    @Contract(pure = true)
    fun enable(flag: Int) = TinyFlags(bitFlags or (1 shl flag.validate()))

    @Contract(pure = true)
    fun disable(flag: Int) = TinyFlags(bitFlags and (1 shl flag.validate()).inv())

    companion object {
        const val MIN_FLAG = 1
        const val MAX_FLAG = 31

        val none inline get() = TinyFlags()

        @JvmStatic
        fun Int.validate(): Int {
            require(this in MIN_FLAG..MAX_FLAG, { "$this is not in the range of ${MIN_FLAG..MAX_FLAG}" })
            return this
        }

        fun enable(flag: Int): TinyFlags {
            return TinyFlags().enable(flag)
        }

        fun enable(a: Int, b: Int): TinyFlags {
            return enable(a).enable(b)
        }

        fun enable(a: Int, b: Int, c: Int): TinyFlags {
            return enable(a, b).enable(c)
        }

        fun enable(vararg flags: Int): TinyFlags {
            enable(59)

            flags.map { enable(it) }.reduce { a, b -> a + b }
            return flags.fold(TinyFlags()) { soFar, next ->
                soFar.enable(next)
            }
        }

        fun fromBitFlags(bitFlags: Int) = TinyFlags(bitFlags)
    }

    override val size: Int
        get() = Integer.bitCount(bitFlags)

    @Contract(pure = true)
    override fun isEmpty(): Boolean = bitFlags <= 0

    @Contract(pure = true)
    override fun contains(element: Int): Boolean = this[element]

    private class Iterator(private var flagsRemaining: Int) : IntIterator() {
        override fun hasNext(): Boolean = flagsRemaining != 0

        override fun nextInt(): Int {
            if (flagsRemaining == 0) {
                throw NoSuchElementException()
            }

            val lowestBit = flagsRemaining and -flagsRemaining
            val index = Integer.numberOfTrailingZeros(lowestBit)
            flagsRemaining = flagsRemaining xor lowestBit
            return index
        }
    }

    @Contract(pure = true)
    override fun iterator(): IntIterator {
        return Iterator(bitFlags)
    }

    @Contract(pure = true)
    fun containsAll(other: TinyFlags) = (bitFlags and other.bitFlags) == other.bitFlags

    @Contract(pure = true)
    override fun containsAll(elements: Collection<Int>): Boolean {
        return when (elements) {
            is TinyFlags -> containsAll(elements)
            else         -> elements.all { contains(it) }
        }
    }
}