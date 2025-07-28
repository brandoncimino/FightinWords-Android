package brava.fightinwords.botlin

import brava.fightinwords.botlin.TinyFlags.Companion.MAX_FLAG
import brava.fightinwords.botlin.TinyFlags.Companion.MIN_FLAG
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Contract

/**
 * A super-efficient way to represent a map of [Boolean]s to the numbers [0][MIN_FLAG] to [31][MAX_FLAG] (inclusive) as a single [Int].
 *
 * Functionally similar to C#'s [[[Flags]]](https://learn.microsoft.com/en-us/dotnet/fundamentals/runtime-libraries/system-flagsattribute), as well as the internal representation of Java's [java.util.EnumSet].
 */
@JvmInline
@Serializable
value class TinyFlags(val flags: Int = 0) : Set<Int> {
    @Contract(pure = true)
    fun hasFlag(flag: Int): Boolean = flags and (1 shl flag.validate()) != 0

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
        enabled -> enable(flag)
        else    -> disable(flag)
    }

    @Contract(pure = true)
    fun enable(flag: Int) = TinyFlags(flags or (1 shl flag.validate()))

    @Contract(pure = true)
    fun disable(flag: Int) = TinyFlags(flags and (1 shl flag.validate()).inv())

    companion object {
        const val MIN_FLAG = 0
        const val MAX_FLAG = 31

        fun Int.validate(): Int {
            require(this in MIN_FLAG..MAX_FLAG)
            return this
        }
    }

    override val size: Int
        get() = Integer.bitCount(flags)

    @Contract(pure = true)
    override fun isEmpty(): Boolean = flags <= 0

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
        return Iterator(flags)
    }

    @Contract(pure = true)
    fun containsAll(other: TinyFlags) = (flags and other.flags) == other.flags

    @Contract(pure = true)
    override fun containsAll(elements: Collection<Int>): Boolean {
        return when (elements) {
            is TinyFlags -> containsAll(elements)
            else         -> elements.all { contains(it) }
        }
    }
}