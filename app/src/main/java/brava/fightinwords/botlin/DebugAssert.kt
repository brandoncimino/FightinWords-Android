package brava.fightinwords.botlin

/**
 * TODO: Control this _compile-time-constant_ from _outside of the source code_.
 *  It sounds like that should be done using some Gradle shenanigans to generate a file.
 */
internal const val DEBUG_ASSERTIONS_ENABLED = false

/**
 * Evaluates [condition] only if [DEBUG_ASSERTIONS_ENABLED] is true.
 *
 * # Why?
 *
 * > See: [KT-7540 "assert" evaluated always](https://www.reddit.com/r/Kotlin/comments/5uslzm/kt7540_assert_evaluated_always/)
 *
 * # How?
 *
 * Because [DEBUG_ASSERTIONS_ENABLED] is a compile-time constant, the always-`false` `if`-branch can be compiled away.
 * This causes the lowered Java method to be:
 * ```java
 *    public static final void debugAssert(@NotNull Function0 condition) {
 *       Intrinsics.checkNotNullParameter(condition, "condition");
 *       int $i$f$debugAssert = 0;
 *    }
 * ```
 *
 * However, that still includes `Intrinsics.checkNotNullParameter()`.
 *
 * Because this method is `inline`d, the `null`-check for `[condition] gets compiled away _at the call site_.
 * This causes the Kotlin code:
 * ```kotlin
 *     private fun createString(): String {
 *         debugAssert { length > 0 }
 *         return toString()
 *     }
 * ```
 *
 * ```java
 *     private final String createString() {
 *       int $i$f$debugAssert = 0;
 *       return this.toString();
 *    }
 * ```
 */
internal inline fun assert(condition: () -> Boolean) {
    if (DEBUG_ASSERTIONS_ENABLED) {
        assert(condition())
    }
}

/**
 * The same as [assert], but with a name that ties it more obviously to C#'s [Debug.Assert(Boolean)](https://learn.microsoft.com/en-us/dotnet/api/system.diagnostics.debug.assert?view=net-9.0).
 */
internal inline fun debugAssert(condition: () -> Boolean) = assert(condition)