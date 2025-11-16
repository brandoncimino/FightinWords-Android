package brava.fightinwords

import brava.fightinwords.botlin.Tabler.Companion.formatTable
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.serializer
import org.assertj.core.api.AbstractIterableAssert
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.IterableAssert
import org.assertj.core.api.ObjectAssert
import org.assertj.core.api.SoftAssertions
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

    inline fun <reified T> assertEquality(
        self: T?,
        other: T?,
        vararg moreOthers: T?,
        expectedEquality: Boolean = true,
        recursiveEquality: Boolean = true,
        simpleEquality: Boolean = true,
        toStringEquality: Boolean = true,
    ) {
        SoftAssertions.assertSoftly {
            with(it) {
                sequenceOf(other, *moreOthers)
                    .forEach { otherWord ->
                        if (recursiveEquality) {

                            val ass = assertThat(self)
                                .describedAs { "Recursive equality" }
                                .usingRecursiveComparison()

                            when (expectedEquality) {
                                true  -> ass.isEqualTo(otherWord)
                                false -> ass.isNotEqualTo(otherWord)
                            }

                        }

                        if (simpleEquality) {
                            val ass = assertThat(self)
                                .describedAs { ".equals() equality" }

                            when (expectedEquality) {
                                true  -> ass.isEqualTo(other)
                                false -> ass.isNotEqualTo(other)
                            }
                        }

                        if (toStringEquality) {
                            val ass = assertThat(self)
                                .describedAs { ".toString() equality" }

                            when (expectedEquality) {
                                true  -> ass.hasToString(other?.toString())
                                false -> ass.doesNotHaveToString(other?.toString())
                            }
                        }
                    }
            }
        }
    }

    inline fun <reified T, reified S> T.assertRoundTrip(
        serializer: (T) -> S,
        deserializer: (S) -> T,
        recursiveEquality: Boolean = true,
        simpleEquality: Boolean = true,
        toStringEquality: Boolean = true,
    ) {
        println(
            """
>> Round trip of 
   -➡ ${T::class.simpleName} 
   ⟹ ${S::class.simpleName}  
   ↩︎ ${T::class.simpleName}  
"""
        )

        val serialized = runCatching { serializer(this) }

        val deserialized = serialized.mapCatching { deserializer(it) }

        listOf(
            "serialized" to serialized::class.simpleName to serialized,
            "deserialized" to deserialized::class.simpleName to deserialized
        )
            .map { it.toList() }
            .formatTable()
            .printing()

        serialized.success()
        deserialized.success()

        assertEquality(
            this,
            deserialized.getOrThrow(),
            recursiveEquality = recursiveEquality,
            simpleEquality = simpleEquality,
            toStringEquality = toStringEquality
        )
    }

    inline fun <reified T> T.assertRoundTrip(
        serialFormat: SerialFormat,
        serializer: KSerializer<T> = serializer<T>(),
        recursiveEquality: Boolean = true,
    ) {
        return when (serialFormat) {
            is StringFormat -> assertRoundTrip(
                { serialFormat.encodeToString(serializer, it) },
                { serialFormat.decodeFromString(serializer, it) },
                recursiveEquality = recursiveEquality
            )

            is BinaryFormat -> {
                assertRoundTrip(
                    { serialFormat.encodeToByteArray(serializer, it) },
                    { serialFormat.decodeFromByteArray(serializer, it) },
                    recursiveEquality = recursiveEquality
                )

                assertRoundTrip(
                    { serialFormat.encodeToHexString(serializer, it) },
                    { serialFormat.decodeFromHexString(serializer, it) },
                    recursiveEquality = recursiveEquality
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

val <T> T.printed: T
    get() {
        println(this)
        return this
    }

fun <T> T.printing(formatter: (T) -> Any? = { it }): T {
    println(this)
    return this
}

infix fun <A, B, C> Pair<A, B>.to(c: C) = Triple(first, second, c)


fun <SELF, ELEMENT> SELF.allSatisfy(
    assertion: Consumer<ELEMENT>,
    vararg moreAssertions: Consumer<ELEMENT>,
): SELF
        where SELF : AbstractIterableAssert<SELF, *, ELEMENT, *> {
    return allSatisfy {
        Assertions.assertThat(it)
            .satisfies(
                assertion,
                *moreAssertions
            )
    }
}

class Equality<T>(
    val function: (T, T) -> Boolean,
    val description: String,
) {

}

class Equalities<T> {
    @PublishedApi
    internal val equalities: MutableMap<String, (T, T) -> Boolean> =
        mutableMapOf()//MutableList<Equality<T>> = mutableListOf()

    fun byEquals() {
        equalities["a == b"] = { a, b -> a == b }
    }

    inline fun <reified K> byKey(
        noinline key: (T) -> K,
        description: String = "Equal by extracted ${K::class.simpleName}",
    ) {
        equalities[description] = { a, b -> key(a) == key(b) }
    }

    fun byString() {
        byKey({ it?.toString() }, "Equal by .toString()")
    }

    fun byComparator(
        comparator: Comparator<T>,
        description: String = "Equal by comparator $comparator",
    ) {
        equalities[description] = { a, b -> comparator.compare(a, b) == 0 }
    }
}

fun <T> assertEquality(
    a: T,
    b: T,
    expectedEquality: Boolean = true,
    equalities: Equalities<T>.() -> Unit,
) {
    val results = Equalities<T>().apply(equalities)
        .equalities
        .mapValues { (description, eq) -> runCatching { eq(a, b) } }

    Assertions.assertThat(results)

        .values()
        .describedAs {
            """
                |🅰 $a
                |🅱 $b
                |are ${
                when (expectedEquality) {
                    true  -> "equal"
                    false -> "NOT equal"
                }
            } according to:
                |${results.formatTable()}
            }
            """.trimMargin()
        }
        .allSatisfy { it.success().isEqualTo(expectedEquality) }
}