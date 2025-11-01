package brava.fightinwords.botlin

import brava.fightinwords.botlin.AliasMatcher.Companion.aliasMatcher
import brava.fightinwords.botlin.AliasMatcher.KnownAliases
import brava.fightinwords.botlin.AliasMatcher.Matchiness.Exact
import brava.fightinwords.botlin.AliasMatcher.Matchiness.Partial
import brava.fightinwords.cartesianProduct
import brava.fightinwords.mangleCase
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import kotlin.enums.enumEntries

class AliasMatcherTest {
    @Test
    fun knownAliasesCanBeInternallyAmbiguous() {
        val knownAliases = KnownAliases(
            "alias",
            listOf("alias_1", "alias_2")
        )

        val candidate = "a"

        val actualMatchiness = knownAliases.getMatchiness(candidate, true)

        Assertions.assertThat(actualMatchiness)
            .isEqualTo(Partial)
    }

    enum class OriginalLocation {
        MyCanonical,
        MyAlias;

        fun getFrom(
            myAliases: KnownAliases,
        ) = when (this) {
            MyCanonical -> myAliases.canonical
            MyAlias     -> myAliases.aliases.first()
        }
    }

    enum class DuplicateLocation {
        MyAlias,
        OtherAlias,
        OtherCanonical;
    }

    fun rejectDuplicateAliases(
        originalLocation: OriginalLocation,
        duplicateLocation: DuplicateLocation,
    ) {
        var myAliases = KnownAliases(
            "myCanonical",
            listOf("myAlias")
        )

        var otherAliases = KnownAliases(
            "otherCanonical",
            listOf("otherAlias")
        )

        val duplicateValue = originalLocation.getFrom(myAliases).uppercase()

        when (duplicateLocation) {
            DuplicateLocation.MyAlias        -> myAliases = myAliases.copy(
                aliases = myAliases.aliases + duplicateValue
            )

            DuplicateLocation.OtherAlias     -> otherAliases = otherAliases.copy(
                aliases = otherAliases.aliases + duplicateValue
            )

            DuplicateLocation.OtherCanonical -> otherAliases = otherAliases.copy(
                canonical = duplicateValue
            )
        }

        val newMatcher = { ignoreCase: Boolean ->
            AliasMatcher(
                mapOf(
                    "me" to myAliases,
                    "other" to otherAliases
                ),
                ignoreCase
            )
        }

        Assertions.assertThatCode {
            newMatcher(false)
        }
            .describedAs { "case-sensitive should NOT reject any duplicates" }
            .doesNotThrowAnyException()

        Assertions.assertThatCode {
            newMatcher(true)
        }
            .describedAs { "case-insensitive SHOULD reject duplicates" }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun rejectDuplicateAliases() {
        val combos = cartesianProduct(
            OriginalLocation.entries,
            DuplicateLocation.entries
        )

        Assertions.assertThat(combos.asIterable())
            .allSatisfy {
                rejectDuplicateAliases(it.first, it.second)
            }
    }

    @Test
    fun enumAliasMatcher() {
        val aliasMatcher = enumEntries<DayOfWeek>().aliasMatcher()

        val expectations = mapOf(
            "m" to partial(MONDAY),
            "th" to partial(THURSDAY),
            "tU" to partial(TUESDAY),
            "t" to ambiguous(TUESDAY, THURSDAY),
            "x" to null,
            " m " to null,
            MONDAY.name to exact(MONDAY),
            MONDAY.name.lowercase() to exact(MONDAY),
            MONDAY.name.mangleCase() to exact(MONDAY),
        )

        Assertions.assertThat(expectations)
            .allSatisfy { candidate, expectation ->
                Assertions.assertThat(aliasMatcher.tryMatch(candidate)).isEqualTo(expectation)

                if (expectation is AliasMatcher.Unambiguous) {
                    Assertions.assertThat(aliasMatcher.requireMatch(candidate))
                        .isEqualTo(expectation.matchedValue)
                } else {
                    Assertions.assertThatCode {
                        aliasMatcher.requireMatch(candidate)
                    }
                        .isNotNull
                }
            }
    }
}

fun <T> partial(matchedValue: T) = AliasMatcher.Unambiguous(matchedValue, Partial)
fun <T> exact(matchedValue: T) = AliasMatcher.Unambiguous(matchedValue, Exact)
fun <T> ambiguous(vararg partialMatches: T) = AliasMatcher.Ambiguous(partialMatches.toList())
