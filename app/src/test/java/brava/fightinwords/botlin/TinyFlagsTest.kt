package brava.fightinwords.botlin

import brava.fightinwords.Besting.assert
import org.assertj.core.api.Assertions
import org.assertj.core.api.IterableAssert
import org.junit.Test
import kotlin.random.Random
import kotlin.random.nextInt

class TinyFlagsTest {
    private val range = TinyFlags.MIN_FLAG..TinyFlags.MAX_FLAG

    @Test
    fun fakeParameterizedTestBecauseGrossness() {
        val cases = listOf(
            listOf(1, 2, 3),
            listOf(1, 5, 19),
        )

        Assertions.assertThat(cases)
            .allSatisfy {
                val tinyFlags: TinyFlags = it.foldFlags()
                assertTinyFlags(tinyFlags, it)
            }
    }

    private fun assertTinyFlags(actualFlags: TinyFlags, expectedFlagged: Collection<Int>) {
        Assertions.assertThat(TinyFlags.MIN_FLAG..TinyFlags.MAX_FLAG)
            .allSatisfy {
                Assertions.assertThat(actualFlags[it])
                    .isEqualTo(expectedFlagged.contains(it))
            }
    }

    data class Flag(val flag: Int, val enabled: Boolean)

    fun Random.nextFlag(): Flag = Flag(nextInt(range), nextBoolean())

    fun randomFlagSequence(count: Int, seed: Int = 99): List<Flag> {
        val rnd = Random(seed);
        return generateSequence { rnd.nextFlag() }
            .take(count)
            .toList()
    }

    @Test
    fun flagOneByOne() {
        sequenceOf(
            listOf(3.on, 4.off, 2.off, 8.on),
            randomFlagSequence(10),
            randomFlagSequence(100),
            range.map { it.on },
            range.map { it.on } + range.map { it.off },
        ).assert()
            .allSatisfy { it: List<Flag> ->
                it.assert().oneByOne()
            }
    }

    val Int.on get() = Flag(this, true)
    val Int.off get() = Flag(this, true)

    private fun IterableAssert<Flag>.oneByOne() {
        var tf = TinyFlags()
        val soFar = mutableListOf<Flag>()

        this.allSatisfy {
            soFar.add(it)
            tf = tf.set(it.flag, it.enabled)
            tf.assertFlag(it.flag, it.enabled)
        }
    }

    fun TinyFlags.assertFlag(flag: Int, expectedState: Boolean) {
        flag.assert().extracting { this[it] }.isEqualTo(expectedState)
    }

    fun Iterable<Int>.foldFlags() = fold(TinyFlags()) { soFar, next -> soFar.enable(next) }

    @Test
    fun plusTestBaby() {
        val f1 = TinyFlags.enable(4)
        val f2 = TinyFlags.enable(5)

        val actual = f1 + f2
        val expected = TinyFlags.enable(4, 5)
        actual.assert().isEqualTo(expected)
    }

    @Test
    fun plusMinusTest() {
        val firstRng = Random(1)
        val firstOns = range.filter { firstRng.nextBoolean() }
        val firstFlags = firstOns.foldFlags()
        val secondRng = Random(2)
        val secondOns = range.filter { secondRng.nextBoolean() }
        val secondFlags = secondOns.foldFlags()

        val plussed = firstFlags + secondFlags
        val toSetPlussed = firstFlags.toSet() + secondFlags.toSet()

        val minussed = firstFlags - secondFlags
        val toSetMinussed = firstFlags.toSet() - secondFlags.toSet()

        val eitherOns = (firstOns + secondOns).toSet()
        val eitherFlags = eitherOns.foldFlags()

        val firstNotSecond = firstOns.filter { it !in secondOns }

        Assertions.assertThat(toSetMinussed)
            .containsExactlyInAnyOrderElementsOf(firstNotSecond)


        assertTinyFlags(plussed, eitherOns)

        plussed.assert()
            .containsExactlyInAnyOrderElementsOf(toSetPlussed)

        Assertions.assertThat(plussed)
            .describedAs("first + second")
            .containsExactlyInAnyOrderElementsOf(toSetPlussed)

        plussed.assert()
            .isEqualTo(eitherFlags)


        Assertions.assertThat(minussed)
            .describedAs("first - second")
            .containsExactlyInAnyOrderElementsOf(toSetMinussed)
    }
}