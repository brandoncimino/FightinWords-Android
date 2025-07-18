package brava.fightinwords.gameplay

import org.assertj.core.api.Assertions
import org.junit.Test

class GalleyTest {
    @Test
    fun selectTest() {
        val galley = Galley<Int>(5);

        Assertions.assertThat(galley.snapshot())
            .isEmpty()

        val expectedSnapshot = mutableListOf<Int>()

        repeat(5) {
            galley.add(it)
            expectedSnapshot.add(it)

            Assertions.assertThat(galley.snapshot())
                .containsExactlyElementsOf(expectedSnapshot)
        }
    }

    @Test
    fun arrayDequeTest() {
        val deque = ArrayDeque<Int>();

        deque.add(1);

        println("deque = ${deque}")
        println("deque.size = ${deque.size}")
    }
}