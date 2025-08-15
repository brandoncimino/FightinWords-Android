package brava.fightinwords.ui

import org.assertj.core.api.Assertions
import org.assertj.core.api.Fail
import org.junit.Test

class JetpackBoostersTest {
    @Test
    fun shrinkToFit_fastPath_maxAlreadyFits() {
        val maxValue = 10
        val shrunk = JetpackBoosters.shrinkToFit(
            maxValue = maxValue,
            minValue = Int.MIN_VALUE,
            checkFit = { it <= maxValue },
            bigShrinker = { Fail.fail() },
            littleGrower = { Fail.fail() },
        )

        Assertions.assertThat(shrunk)
            .isEqualTo(maxValue)
    }

    @Test
    fun shrinkToFit_fastPath_minStillTooBig() {
        val minValue = 10
        val shrunk = JetpackBoosters.shrinkToFit(
            maxValue = Int.MAX_VALUE,
            minValue = minValue,
            checkFit = { it < minValue },
            bigShrinker = { Fail.fail() },
            littleGrower = { Fail.fail() },
        )

        Assertions.assertThat(shrunk)
            .isEqualTo(minValue)
    }

    @Test
    fun shrinkToFit_mediumPath_reachShrinkLimit() {
        val maxValue = 10
        val shrinkrement = 1
        val maxShrinks = 5
        val expectedShrunk = maxValue - (shrinkrement * maxShrinks)
        val shrunk = JetpackBoosters.shrinkToFit(
            maxValue = maxValue,
            minValue = Int.MIN_VALUE,
            checkFit = { it == Int.MIN_VALUE },
            bigShrinker = { it - shrinkrement },
            littleGrower = { Fail.fail() },
            maxShrinks = maxShrinks
        )

        Assertions.assertThat(shrunk)
            .isEqualTo(expectedShrunk)
    }

    @Test
    fun shrinkToFit_mediumPath_reachGrowLimit() {
        val maxValue = 100
        val shrinkrement = 10
        val growcrement = 1
        val maxGrows = 5
        val expectedShrunk = maxValue - (shrinkrement) + (growcrement * maxGrows)

        var growCount = 0;

        val shrunk = JetpackBoosters.shrinkToFit(
            maxValue = maxValue,
            minValue = Int.MIN_VALUE,
            checkFit = { it < maxValue },
            bigShrinker = {
                growCount += 1
                it - shrinkrement
            },
            littleGrower = { it + growcrement },
            maxGrows = maxGrows,
        )

        Assertions.assertThat(growCount)
            .describedAs("growCount")
            .isEqualTo(maxGrows)

        Assertions.assertThat(shrunk)
            .describedAs("final result")
            .isEqualTo(expectedShrunk)
    }

    data class SimpleScenario(
        val maxValue: Int,
        val minValue: Int = Int.MIN_VALUE,
        val targetValue: Int = 0,
        val shrinkrement: Int,
        val growcrement: Int,
        val expected: Int,
    ) {
        fun run() {
            val shrunk = JetpackBoosters.shrinkToFit(
                maxValue = maxValue,
                minValue = Int.MIN_VALUE,
                checkFit = { it < targetValue },
                bigShrinker = { it - shrinkrement },
                littleGrower = { it + growcrement },
            )

            Assertions.assertThat(shrunk)
                .isEqualTo(expected)
        }
    }

    @Test
    fun shrinkToFit() {
        val scenarios = listOf(
            SimpleScenario(
                maxValue = 10,
                shrinkrement = 7,
                growcrement = 3,
                expected = -1
            ),
            SimpleScenario(
                maxValue = 10,
                shrinkrement = 11,
                growcrement = 2,
                expected = -1
            )
        )

        Assertions.assertThat(scenarios)
            .allSatisfy { it.run() }
    }
}