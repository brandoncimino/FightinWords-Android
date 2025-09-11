package brava.fightinwords.botlin

import brava.fightinwords.botlin.TinyFlags.Companion.MAX_FLAG
import brava.fightinwords.botlin.TinyFlags.Companion.MIN_FLAG
import brava.fightinwords.botlin.TinyFlags.Companion.enable
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Contract

//fun TinyFlags() = TinyFlags.none

/**
 * A super-efficient way to represent a map of [Boolean]s to the numbers [0][MIN_FLAG] to [31][MAX_FLAG] (inclusive) as a single [Int].
 *
 * Functionally similar to C#'s [[[Flags]]](https://learn.microsoft.com/en-us/dotnet/fundamentals/runtime-libraries/system-flagsattribute)
as well as the internal representation of Java's [java.util.EnumSet].
 */
@JvmInline
@Serializable
value class TinyFlags(val bitFlags: Int = 0) : Set<Int> {
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
        const val MIN_FLAG = 0
        const val MAX_FLAG = 31

        const val MAX_FLAG_COUNT = 32

        const val noneInt = 0
        const val allInt = -1

        val none inline get() = TinyFlags(noneInt)
        val all inline get() = TinyFlags(allInt)

        val empty inline get() = fromBitFlags(noneInt)

        fun TinyFlags.isNothing() = this == empty

        @JvmStatic
        private fun Int.validate(): Int {
            require(this in MIN_FLAG..MAX_FLAG, { "$this is not in the range of ${MIN_FLAG..MAX_FLAG}" })
            return this
        }

        @Contract(pure = true)
        fun enable(flag: Int): TinyFlags {
            return TinyFlags().enable(flag)
        }

        @Contract(pure = true)
        fun enable(a: Int, b: Int): TinyFlags {
            return enable(a).enable(b)
        }

        @Contract(pure = true)
        fun enable(a: Int, b: Int, c: Int): TinyFlags {
            return enable(a, b).enable(c)
        }

        @Contract(pure = true)
        fun enable(vararg flags: Int): TinyFlags {
            flags.map { enable(it) }.reduce { a, b -> a + b }
            return flags.fold(TinyFlags()) { soFar, next ->
                soFar.enable(next)
            }
        }

        fun fromBitFlags(bitFlags: Int) = TinyFlags(bitFlags)

        /**
         * @return a [brava.fightinwords.botlin.TinyFlags] where the first [flagCount] flags (i.e. `0 until flagCount`) are [enable]d.
         */
        fun first(flagCount: Int): TinyFlags {
            val bitFlags = when (flagCount) {
                0    -> 0
                1    -> 1
                2    -> 3
                3    -> 7
                4    -> 15
                5    -> 31
                6    -> 63
                7    -> 127
                8    -> 255
                9    -> 511
                10   -> 1023
                11   -> 2047
                12   -> 4095
                13   -> 8191
                14   -> 16383
                15   -> 32767
                16   -> 65535
                17   -> 131071
                18   -> 262143
                19   -> 524287
                20   -> 1048575
                21   -> 2097151
                22   -> 4194303
                23   -> 8388607
                24   -> 16777215
                25   -> 33554431
                26   -> 67108863
                27   -> 134217727
                28   -> 268435455
                29   -> 536870911
                30   -> 1073741823
                31   -> 2147483647
                32   -> -1
                else -> throw IllegalArgumentException()
            }

            return TinyFlags(bitFlags)
        }
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