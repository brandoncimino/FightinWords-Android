package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.Word.Companion.indices
import brava.fightinwords.ui.submissions.appendCodePoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.cbor.CborEncoder
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.function.IntFunction
import kotlin.math.min

/**
 * Let’s estimate the **serialized size in CBOR** for both approaches, given your constraints:
 *
 * * **Alphabet**: `a–z` (1 byte/char in UTF-8).
 * * **Length**: up to **12 code points (24 characters max)**.
 * * **Strategy 1**: Pack into a single unsigned `Long` (≤ 2⁶⁰-1) when length ≤ 12, otherwise fall back to a text string. Include **one explicit discriminator** so the decoder knows whether the value is an integer or a string.
 * * **Strategy 2**: Always encode as a text string.
 *
 * ---
 *
 * ## 1. CBOR size formulas
 *
 * | Type             | Size formula                                                                                  |
 * | ---------------- | --------------------------------------------------------------------------------------------- |
 * | **Unsigned Int** | 1 byte (0–23), 2 bytes (≤255), 3 bytes (≤65 535), 5 bytes (≤4 294 967 295), 9 bytes (≤2⁶⁴-1). |
 * | **Text String**  | 1 byte header + `N` bytes content (`N` = characters here).                                    |
 *
 * The packed `Long` will top out at 12 chars × 5 bits = **60 bits**, so it always fits in 9 bytes.
 *
 * ---
 *
 * ## 2. Indicator overhead
 *
 * If you wrap the value as a CBOR **map with a tag** such as
 * `{ "t": 0/1, "v": <data> }`:
 *
 * * Key `"t"` = 1 byte header + 1 char = 2 bytes.
 * * Boolean 0/1 = 1 byte.
 * * Key `"v"` = 1 byte header + 1 char = 2 bytes.
 * * Payload = size of packed `Long` or text.
 *
 * So the indicator costs **≈ 5 bytes** in the simplest object form.
 * (You can shave a byte with a CBOR array `[tag, value]` → 1 byte header + payload, i.e. **\~1 byte overhead**.)
 *
 * I’ll assume the compact array form: `[_indicator_, _value_]` → **1 byte array header + 1 byte indicator = 2 bytes** overhead.
 *
 * ---
 *
 * ## 3. Per-string comparison (using array overhead)
 *
 * | String length (chars)   | Plain text size       | Packed long size + overhead                  |
 * | ----------------------- | --------------------- | -------------------------------------------- |
 * | 1                       | 1 + 1 = **2**         | 1 (int) + 2 overhead = **3**                 |
 * | 2                       | 1 + 2 = **3**         | 2 + 2 = **4**                                |
 * | 3                       | 1 + 3 = **4**         | 2–3 + 2 = **4–5**                            |
 * | 4–5                     | 1 + 4–5 = **5–6**     | 3 + 2 = **5**                                |
 * | 6–7                     | 1 + 6–7 = **7–8**     | 5 + 2 = **7**                                |
 * | 8–12                    | 1 + 8–12 = **9–13**   | 9 + 2 = **11**                               |
 * | 13–24 (must use string) | 1 + 13–24 = **14–25** | 1 + 13–24 = **14–25** (falls back to string) |
 *
 * *For lengths ≥4–5 the packed integer plus small indicator is roughly equal or slightly smaller than the raw string.
 * For 1–3 chars it’s actually **larger** because of the indicator overhead.*
 *
 * ---
 *
 * ## 4. Interpretation
 *
 * * **Short words (1–3 chars)**: plain text wins.
 * * **Medium words (4–7 chars)**: essentially a tie.
 * * **Long “tiny words” (8–12 chars)**: packed long + small indicator is a few bytes smaller.
 * * **13–24 chars**: you must store as text anyway.
 *
 * Your actual savings depend on the distribution of lengths.
 * Example: if 80 % of your words are 1–3 chars, the mixed scheme will **increase** average size.
 *
 * ---
 *
 * ## 5. Other trade-offs
 *
 * * **CPU**: Packing/unpacking 5-bit chunks adds a little cost.
 * * **Readability**: Packed integers are opaque in CBOR diagnostic tools.
 * * **Schema complexity**: Mixed representation requires a custom serializer.
 *
 * ---
 *
 * ### **Recommendation**
 *
 * * If your dataset skews **longer** (many 8–12 char strings) and you care about every byte,
 *   a *compact* indicator (e.g., a CBOR array `[0,value]` for int, `[1,value]` for string) can save a few bytes overall.
 * * If you have many **short words (≤3 chars)** or you value simplicity/readability,
 *   **always using text strings is usually better**—the extra logic and indicator overhead won’t pay off.
 *
 * Unless profiling shows a clear size win, most teams stick with **plain CBOR strings** for simplicity and human-readability.
 *
 */
@Serializable(Word.Serializer::class)
sealed interface Word : Comparable<Word> {
    object Serializer : KSerializer<Word> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            Word::class.qualifiedName!!,
            PrimitiveKind.STRING
        )

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(
            encoder: Encoder,
            value: Word,
        ) {
            LongArraySerializer()
            val cborEncoder = encoder as CborEncoder
//            encoder.encodeSerializableValue(ByteArraySerializer(), value)
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Word {
            return decoder.decodeString().toWord()
        }
    }

    val length: Int

    fun isEmpty(): Boolean = length == 0

    operator fun get(index: Int): Letter

    companion object {
        fun Iterable<Letter>.toWord(): Word {
            return when (this) {
                is Word -> this
                else -> WordLetters(this.toList())
            }
        }

        fun String.toWord(): Word {
            return StringWord(this)
        }

        val Word.indices inline get() = 0 until length

        /**
         * Compares two words [lexicographically](https://en.wikipedia.org/wiki/Lexicographic_order).
         */
        inline fun <reified T : Word> T.lexicographicalCompare(other: T): Int {
            return ListImplementation.lexicographicalCompare(
                length,
                other.length
            ) { letterIndex ->
                this[letterIndex].compareTo(other[letterIndex])
            }
        }

        /**
         * Compares two words in [shortlex order](https://en.wikipedia.org/wiki/Shortlex_order).
         */
        fun Word.shortlexCompare(other: Word): Int {
            if (this === other) {
                return 0
            }

            return ListImplementation.shortlexCompare(
                length,
                other.length
            ) { letterIndex ->
                this[letterIndex].compareTo(other[letterIndex])
            }
        }
    }

    override fun compareTo(other: Word): Int {
        return lexicographicalCompare(other)
    }
}

@Serializable
@JvmInline
value class StringWord(val stringValue: String) : Word {
    override val length: Int
        get() = stringValue.length

    override fun get(index: Int): Letter {
        return stringValue.codePoints()
            .skip(index.toLong())
            .findFirst()
            .orElseThrow()
            .toLetter()
    }

    override fun toString(): String {
        return stringValue
    }
}

private class LettersString(val stringValue: String) : AbstractList<Letter>() {
    override val size: Int = stringValue.length
    override fun get(index: Int): Letter = TinyLetter(stringValue[index])
}

/**
 * A collection of [Letter]s.
 *
 * TODO: Make [TinyWord] more interchangeable with [Word]. Options include:
 *   - Make [Word] into a `sealed interface`
 */
//@Serializable(WordLetters.Serializer::class)
@JvmInline
value class WordLetters(val letters: List<Letter>) : List<Letter> by letters, Comparable<Word>,
                                                     Word {

    override val length: Int
        get() = size

    @Suppress("DEPRECATION")
    @Deprecated("This is a mandatory override of a deprecated Java method")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> =
        super<List>.toArray(generator)

    override fun toString(): String {
        return when (this.letters) {
            is LettersString -> this.letters.stringValue
            else             -> this.joinToString(separator = "") { it.toString() }
        }
    }

    fun compareTo(other: WordLetters): Int {
        if (this.letters is LettersString && other.letters is LettersString) {
            return this.letters.stringValue.compareTo(other.letters.stringValue)
        }

        val shorter = min(this.length, other.length)

        for (i in 0 until shorter) {
            val comparison = this.letters[i].compareTo(other.letters[i])
            if (comparison != 0) {
                return comparison
            }
        }

        return 0
    }

    override fun containsAll(elements: Collection<Letter>): Boolean {
        return letters.containsAll(elements)
    }

    override fun indexOf(element: Letter): Int {
        return letters.indexOf(element)
    }

    override fun lastIndexOf(element: Letter): Int {
        return letters.lastIndexOf(element)
    }

    override fun isEmpty(): Boolean {
        return letters.isEmpty()
    }

    override fun listIterator(): ListIterator<Letter> {
        return letters.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<Letter> {
        return letters.listIterator(index)
    }

    override fun contains(element: Letter): Boolean {
        return letters.contains(element)
    }

    override fun iterator(): Iterator<Letter> {
        return letters.iterator()
    }

    override val size: Int
        get() = letters.size

    override fun subList(fromIndex: Int, toIndex: Int): List<Letter> {
        return letters.subList(fromIndex, toIndex)
    }
}

fun Appendable.append(word: Word): Appendable {
    if (word.isEmpty()) {
        return this
    }

    return when (word) {
        is StringWord -> append(word.stringValue)
        is TinyWord   -> {
            for (i in word.indices) {
                append(word[i].character)
            }
            this
        }

        else          -> {
            for (i in word.indices) {
                appendCodePoint(word[i].codePoint)
            }
            this
        }
    }
}