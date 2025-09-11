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

    private fun assertEnabled(
        actualFlags: TinyFlags,
        expectedEnabled: Iterable<Int>,
    ) {
        val actualEnabled = actualFlags.toList()
        Assertions.assertThat(actualEnabled)
            .describedAs { "Enabled flags of $actualFlags" }
            .containsExactlyInAnyOrderElementsOf(expectedEnabled)
    }

    private fun assertTinyFlags(actualFlags: TinyFlags, expectedFlagged: Iterable<Int>) {
        Assertions.assertThat(TinyFlags.MIN_FLAG..TinyFlags.MAX_FLAG)
            .allSatisfy { flag ->
                Assertions.assertThat(actualFlags[flag])
                    .describedAs { "flag $flag enabled in ${actualFlags.describe()}" }
                    .isEqualTo(expectedFlagged.contains(flag))
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

    fun expectedFirstFlags(flagCount: Int): TinyFlags {
        return (0 until flagCount).foldFlags()
    }

    fun firstXTest(flagCount: Int) {
        val actual = TinyFlags.first(flagCount)
        val expectedOn = 0 until flagCount

        assertEnabled(actual, expectedOn)

        assertTinyFlags(actual, expectedOn)
    }

    @Test
    fun generateFirstMethod() {
        val caseBranches = (0..TinyFlags.MAX_FLAG_COUNT).joinToString(separator = "\n") {
            "$it -> ${expectedFirstFlags(it).bitFlags}"
        }
        println(caseBranches)
    }

    @Test
    fun allTest() {
        val actual = TinyFlags(Int.MAX_VALUE)
        val expectedFlagged = range.toList()

        assertTinyFlags(actual, expectedFlagged)
    }

    @Test
    fun firstXTest() {
        Assertions.assertThat(0..TinyFlags.MAX_FLAG_COUNT)
            .allSatisfy { flagCount ->
                firstXTest(flagCount)
            }
    }

    fun TinyFlags.describe(): String {
        return "$this ${toList()}"
    }
}