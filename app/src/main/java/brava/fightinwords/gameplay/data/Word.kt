package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.botlin.smartForEach
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.TinyWord.Companion.asTinyWord
import brava.fightinwords.gameplay.data.Word.Companion.indices
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
            if (this is Word) {
                return this
            }

            if (this is List) {
                val attemptedTiny = TinyWord.tryCreate(
                    { this[it].codePoint },
                    0,
                    lastIndex
                )

                if (attemptedTiny != null) {
                    return attemptedTiny
                }
            }

            return StringWord(
                buildString {
                    smartForEach {
                        appendCodePoint(it.codePoint)
                    }
                }
            )
        }

        fun String.toWord(): Word {
            return asTinyWord() ?: StringWord(this)
        }

        val Word.indices inline get() = 0 until length

        /**
         * Compares two words [lexicographically](https://en.wikipedia.org/wiki/Lexicographic_order).
         */
        fun Word.lexicographicalCompareTo(other: Word): Int {
            return lexicographicalCompare(this, other)
        }

        /**
         * Compares two words [lexicographically](https://en.wikipedia.org/wiki/Lexicographic_order).
         */
        fun lexicographicalCompare(a: Word, b: Word): Int {
            return ListImplementation.lexicographicalCompare(
                a.length,
                b.length
            ) { letterIndex ->
                a[letterIndex].compareTo(b[letterIndex])
            }
        }

        /**
         * Compares two words in [shortlex order](https://en.wikipedia.org/wiki/Shortlex_order).
         */
        fun Word.shortlexCompareTo(other: Word): Int {
            return shortlexCompare(this, other)
        }

        /**
         * Compares two words in [shortlex order](https://en.wikipedia.org/wiki/Shortlex_order).
         */
        fun shortlexCompare(a: Word, b: Word): Int {
            if (a === b) {
                return 0
            }

            return ListImplementation.shortlexCompare(
                a.length,
                b.length
            ) { letterIndex ->
                a[letterIndex].compareTo(b[letterIndex])
            }
        }

        /**
         * @return An immutable [List] of my [Letter]s.
         */
        fun Word.asList(): List<Letter> {
            return object : AbstractList<Letter>() {
                override val size: Int
                    get() = length

                override fun get(index: Int): Letter {
                    return this@asList[index]
                }
            }
        }
    }

    override fun compareTo(other: Word): Int {
        return this@Word.lexicographicalCompareTo(other)
    }
}

@Serializable
@JvmInline
internal value class StringWord(val stringValue: String) : Word {
    override val length: Int
        get() = stringValue.codePointCount(0, stringValue.length)

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

fun <T : Appendable> T.appendWord(word: Word): T {
    if (word.isEmpty()) {
        return this
    }

    return when (word) {
        is StringWord     -> {
            append(word.stringValue)
            this
        }
        is TinyWord   -> {
            for (i in word.indices) {
                append(word[i].character)
            }
            this
        }
    }
}