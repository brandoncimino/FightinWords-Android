package brava.fightinwords

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import org.assertj.core.api.Assertions
import org.assertj.core.api.IterableAssert
import org.assertj.core.api.ObjectAssert
import java.util.function.Consumer

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