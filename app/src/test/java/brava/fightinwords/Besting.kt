package brava.fightinwords

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