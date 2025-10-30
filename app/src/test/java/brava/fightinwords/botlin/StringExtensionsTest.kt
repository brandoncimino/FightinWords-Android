package brava.fightinwords.botlin

import org.assertj.core.api.Assertions
import org.junit.Test

class StringExtensionsTest {
    class NonIterableList<T>(val stuff: List<T>) : AbstractList<T>() {
        override val size: Int get() = stuff.size

        override fun get(index: Int): T {
            return stuff[index]
        }

        override fun iterator(): MutableIterator<T> {
            throw UnsupportedOperationException();
        }
    }

    @Test
    fun containsDoesNotCreateListIterator() {
        val stuff = NonIterableList(listOf("a", "b", "c"))

        Assertions.assertThatCode {
            stuff.contains("d")
        }
            .describedAs { "The default `contains` method should call `iterator()`" }
            .isInstanceOf(UnsupportedOperationException::class.java)

        Assertions.assertThat(stuff.contains("C", false))
            .isEqualTo(false)

        Assertions.assertThat(stuff.contains("C", true))
            .isEqualTo(true)
    }
}