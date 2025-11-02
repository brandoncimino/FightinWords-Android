package brava.fightinwords

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.IterableAssert
import org.assertj.core.api.ObjectAssert
import java.util.function.Consumer
import kotlin.random.Random

object Besting {

    fun <T> T.assert(): ObjectAssert<T> {
        return Assertions.assertThat(this)
    }

    fun <E> Iterable<E>.assert(): IterableAssert<E> = Assertions.assertThat(this)
    fun <E> Sequence<E>.assert(): IterableAssert<E> = asIterable().assert()

    /**
     * Some kinda magic nonsense that lets you coerce a `(T) -> Unit` into a [Consumer]
     *
     * It seems like Kotlin is OK coercing [Function]s into Java [FunctionalInterface] _parameters_,
     * but not to variables or `return` values...very suspicious.
     */
    private fun <T> asConsumer(consumer: Consumer<T>): Consumer<T> {
        return consumer
    }

    fun <T> ((T) -> Any).asConsumer(): Consumer<T> {
        return Consumer<T> { this(it) }
    }

    fun <T> T.ass(nickname: String) = Assertions.assertThat(this).`as`(nickname)

    inline fun <reified T, reified S> T.assertRoundTrip(
        serializer: (T) -> S,
        deserializer: (S) -> T,
    ) {
        println(
            """
>> Round trip of 
   -➡ ${T::class.simpleName} 
    ↳ ${S::class.simpleName}  
    ↳ ${T::class.simpleName}  
"""
        )

        val serialized = serializer(this)

        println(
            "serialized = [${serialized::class}] ${
                when (serialized) {
                    is ByteArray -> serialized.toList()
                    else         -> serialized
                }
            }"
        )

        val deserialized = deserializer(serialized)
        println("deserialized = [${deserialized?.javaClass}] ${deserialized}")

        println("obj == deserialized = ${this == deserialized}")

        Assertions.assertThat(deserialized)
            .isInstanceOf(T::class.java)
            .isEqualTo(this)
            .usingRecursiveAssertion()
            .isEqualTo(this)
    }

    inline fun <reified T> T.assertRoundTrip(
        serialFormat: SerialFormat,
        serializer: KSerializer<T> = serializer<T>(),
    ) {
        return when (serialFormat) {
            is StringFormat -> assertRoundTrip(
                { serialFormat.encodeToString(serializer, it) },
                { serialFormat.decodeFromString(serializer, it) }
            )

            is BinaryFormat -> {
                assertRoundTrip(
                    { serialFormat.encodeToByteArray(serializer, it) },
                    { serialFormat.decodeFromByteArray(serializer, it) }
                )

                assertRoundTrip(
                    { serialFormat.encodeToHexString(serializer, it) },
                    { serialFormat.decodeFromHexString(serializer, it) }
                )
            }

            else            -> throw UnsupportedOperationException()
        }
    }
}

data class Nicknamed<T>(val value: T, val nickname: String) {
    companion object {
        inline fun <reified T> T.nicknamed(nicknamer: (T) -> Any?) =
            Nicknamed(this, "[${T::class.simpleName}]${nicknamer(this)}")
    }

    override fun toString(): String {
        return nickname
    }
}

fun <A, B> cartesianProduct(
    a: Iterable<A>,
    b: Iterable<B>,
): Sequence<Pair<A, B>> {
    return sequence {
        for (aItem in a) {
            for (bItem in b) {
                yield(aItem to bItem)
            }
        }
    }
}

fun <A, B, C> cartesianProduct(
    a: Iterable<A>,
    b: Iterable<B>,
    c: Iterable<C>,
): Sequence<Triple<A, B, C>> {
    return sequence {
        for (aItem in a) {
            for (bItem in b) {
                for (cItem in c) {
                    yield(Triple(aItem, bItem, cItem))
                }
            }
        }
    }
}

fun CharSequence.mangleCase(random: Random = Random): String {
    return buildString {
        this@mangleCase.codePoints()
            .map {
                when (random.nextBoolean()) {
                    true -> Character.toUpperCase(it)
                    false -> Character.toLowerCase(it)
                }
            }
            .forEach { appendCodePoint(it) }
    }
}

//region Result extensions

inline fun <reified T> Result<T>.prettyName() = "${this::class.simpleName}<${T::class.simpleName}>"

inline fun <reified T> Result<T>.success(): ObjectAssert<T> {
    return Assertions.assertThat(
        this.getOrElse {
            Assertions.fail(
                "Expected the ${prettyName()} to have succeeded!",
                it
            )
        }
    )
}

inline fun <reified T> Result<T>.failure(): AbstractThrowableAssert<*, *> {
    return Assertions.assertThatCode {
        this.getOrThrow()
    }.describedAs { prettyName() }
}

//endregion