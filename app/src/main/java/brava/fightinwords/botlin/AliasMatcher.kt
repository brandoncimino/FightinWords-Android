package brava.fightinwords.botlin

import brava.fightinwords.botlin.AliasMatcher.Companion.create
import brava.fightinwords.botlin.AliasMatcher.Companion.getMatchiness
import brava.fightinwords.botlin.AliasMatcher.KnownAliases
import brava.fightinwords.botlin.AliasMatcher.Matchiness
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import kotlin.enums.EnumEntries

/**
 * Looks up [T] values based on their [KnownAliases].
 */
class AliasMatcher<T : Any>(
    /**
     * The possible [T]s and their [KnownAliases].
     *
     * 📎 Amongst all of [KnownAliases]
     */
    val aliasMap: ImmutableMap<T, KnownAliases>,
    /**
     * If `true`, then we will perform string comparisons in a case-insensitive way,
     * e.g. via [CharSequence.contentEquals]`(other: CharSequence?, ignoreCase: Boolean)`.
     *
     * 📎 This value may determine whether or not the [aliasMap] is valid.
     * For example, given the following [KnownAliases]:
     * ```kotlin
     * KnownAliases(canonical = "a", aliases = listOf("A"))
     * KnownAliases(canonical = "b", aliases = listOf("B"))
     * ```
     */
    val ignoreCase: Boolean,
) {
    constructor(
        allKnownAliases: Map<T, KnownAliases>,
        ignoreCase: Boolean = true,
    ) : this(
        ImmutableMap.copyOf(allKnownAliases),
        ignoreCase
    )

    init {
        validate(aliasMap.values, ignoreCase)
    }

    enum class Matchiness {
        Exact,
        Partial
    }

    /**
     * The [T] values that had matching [KnownAliases].
     *
     * 📎 Note to future Brandon:
     * > I tried really hard to do something fancy with a "typed nothing" -
     * some kind of `object NoMatch` that can be used as any [MatchResult]`<*>` -
     * but any janky way I could get it to work was indeed quite jank;
     * it made much, much more sense to just use a nullable `MatchResult<T>?`.
     * >
     * > -- Past Brandon, Oct. 31, 2025
     */
    sealed interface MatchResult<T>

    /**
     * Of the [KnownAliases], ***EITHER:***
     * - One was an [Matchiness.Exact] match, ***OR***
     * - There was ***exactly 1*** [Matchiness.Partial] match
     *
     * 📎 It _should_ be impossible to find multiple [Matchiness.Exact] matches.
     */
    data class Unambiguous<T>(val matchedValue: T, val matchiness: Matchiness) : MatchResult<T>

    /**
     * Of the [KnownAliases], ***BOTH:***
     * - There were 0 [Matchiness.Exact] matches, ***AND***
     * - There was ***more than 1*** [Matchiness.Partial] match,
     */
    data class Ambiguous<T>(val partialMatches: List<T>) : MatchResult<T>

    /**
     * Compares [KnownAliases.getMatchiness] of [nameOrAbbreviation] against all of my [aliasMap] entries:
     *
     * - If there was 1 [Matchiness.Exact] match, it's [Unambiguous] _(regardless of any [Matchiness.Partial] matches)_
     * - If there was exactly 1 [Matchiness.Partial] match, it's [Unambiguous]
     * - If there were 2+ [Matchiness.Partial] matches, it's [Ambiguous]
     * - If there were 0 matches, return `null`
     *
     * Or, expressed as a wonderful [C# positional pattern](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/operators/patterns#positional-pattern):
     *
     * ```csharp
     * (exactMatches, partialMatches) switch {
     *   (   1,   _ ) => Unambiguous
     *   (   0,   1 ) => Unambiguous
     *   (   0, > 1 ) => Ambiguous
     *   (   0,   0 ) => null
     *   ( > 1,   _ ) => ❌ This should have been impossible!
     *   ( < 0, < 0 ) => ⏭️ This is _definitely_ impossible
     * }
     * ```
     *
     * @see requireMatch
     */
    fun tryMatch(nameOrAbbreviation: CharSequence): MatchResult<T>? {
        val partialMatches = mutableListOf<T>()
        for ((matchedValue, knownAliases) in aliasMap) {
            val matchiness = knownAliases.getMatchiness(nameOrAbbreviation, ignoreCase)
            when (matchiness) {
                Matchiness.Exact   -> return Unambiguous(matchedValue, Matchiness.Exact)
                Matchiness.Partial -> partialMatches.add(matchedValue)
                null               -> continue
            }
        }

        return when (partialMatches.size) {
            0    -> null
            1    -> Unambiguous(partialMatches[0], Matchiness.Partial)
            else -> Ambiguous(partialMatches)
        }
    }

    /**
     * Finds the [nameOrAbbreviation]'s single [Unambiguous.matchedValue] among my [aliasMap] entries.
     *
     * @throws IllegalArgumentException If the [nameOrAbbreviation] was [Ambiguous]
     * @throws NoSuchElementException If there weren't any matches
     *
     * @see tryMatch
     */
    fun requireMatch(nameOrAbbreviation: CharSequence): T {
        return when (val result = tryMatch(nameOrAbbreviation)) {
            is Unambiguous<T> -> result.matchedValue
            is Ambiguous<T>   -> throw IllegalArgumentException("The string `$nameOrAbbreviation` is ambiguous. Partial matches include: ${result.partialMatches.joinToString { "$it → ${aliasMap[it]}" }}")
            null              -> throw NoSuchElementException("The string `$nameOrAbbreviation` didn't match anything in: $this")
        }
    }

    companion object {
        /**
         * Constructs an [AliasMatcher] by extracting [KnownAliases] from each of the [values].
         *
         * @param values The actual [T] values that you want to find.
         * @param canonicalName Extracts the [KnownAliases.canonical] from a [T].
         * @param aliases Extracts the [KnownAliases.aliases] from a [T].
         * @param ignoreCase See [AliasMatcher.ignoreCase].
         */
        inline fun <T : Any> create(
            values: Iterable<T>,
            canonicalName: (T) -> String,
            aliases: (T) -> List<String> = { listOf() },
            ignoreCase: Boolean = true,
        ): AliasMatcher<T> {
            return create(
                values,
                { value ->
                    KnownAliases(
                        canonicalName(value),
                        aliases(value)
                    )
                },
                ignoreCase
            )
        }

        /**
         * Constructs an [AliasMatcher] by extracting [KnownAliases] from each of the [values].
         *
         * @param values The actual [T] values that you want to find.
         * @param getKnownAliases Extracts the [KnownAliases] from a [T].
         * @param ignoreCase See [AliasMatcher.ignoreCase].
         */
        inline fun <T : Any> create(
            values: Iterable<T>,
            getKnownAliases: (T) -> KnownAliases,
            ignoreCase: Boolean = true,
        ): AliasMatcher<T> {
            val aliasMap = ImmutableMap.builder<T, KnownAliases>()

            for (value in values) {
                val knownAliases = getKnownAliases(value)

                aliasMap.put(value, knownAliases)
            }

            return AliasMatcher(
                aliasMap.buildOrThrow(),
                ignoreCase
            )
        }

        /**
         * A convenience method to [create] an [AliasMatcher] based on [EnumEntries].
         *
         * @param canonicalName Extracts the [KnownAliases.canonical] from an [E].
         * @param aliases Extracts the [KnownAliases.aliases] from an [E].
         * @param ignoreCase See [AliasMatcher.ignoreCase].
         */
        fun <E : Enum<E>> EnumEntries<E>.aliasMatcher(
            canonicalName: (E) -> String = { it.name },
            aliases: (E) -> List<String> = { listOf() },
            ignoreCase: Boolean = true,
        ) = create(
            this,
            canonicalName,
            aliases,
            ignoreCase
        )

        /**
         * Determines how closely [nameOrAbbreviation] matches the ***BEGINNING*** of [fullName].
         *
         * Examples:
         *
         * - [getMatchiness]`("mon", "monday")` ⇒ [Matchiness.Partial]
         * - [getMatchiness]`("monday", "monday")` ⇒ [Matchiness.Exact]
         * - [getMatchiness]`("MONDAY", "monday", ignoreCase = false)` ⇒ `null`
         * - [getMatchiness]`("MONDAY", "monday", ignoreCase = true)` ⇒ [Matchiness.Exact]
         * _(the [Matchiness] takes [ignoreCase] into account)_
         * - [getMatchiness]`("monday", "mon")` ⇒ `null`
         * _(the [nameOrAbbreviation] should be the "smaller" string)_
         * - [getMatchiness]`("day", "monday")` ⇒ `null`
         * _(we only look at the **start** of the [fullName])_
         *
         * @param nameOrAbbreviation The "smaller" string.
         * @param fullName The "bigger" string, which might [startsWith] the [nameOrAbbreviation].
         *
         * @return If:
         * - The strings are [contentEquals] ⇒ [Matchiness.Exact]
         * - The [fullName].[startsWith] the [nameOrAbbreviation] ⇒ [Matchiness.Partial]
         * - else ⇒ `null`
         */
        fun getMatchiness(
            nameOrAbbreviation: CharSequence,
            fullName: CharSequence,
            ignoreCase: Boolean = true,
        ): Matchiness? {
            if (fullName.startsWith(nameOrAbbreviation, ignoreCase)) {
                return when (fullName.length == nameOrAbbreviation.length) {
                    true  -> Matchiness.Exact
                    false -> Matchiness.Partial
                }
            }

            return null
        }


        /**
         * Gets the
         */
        fun getMatchiness(
            nameOrAbbreviation: CharSequence,
            canonicalName: CharSequence,
            aliases: Iterable<CharSequence>,
            ignoreCase: Boolean = false,
        ): Matchiness? {
            return aliases.fold(
                getMatchiness(nameOrAbbreviation, canonicalName, ignoreCase),
            ) { matchinessSoFar, alias ->
                getStrongerMatchiness(
                    nameOrAbbreviation,
                    alias,
                    ignoreCase,
                    matchinessSoFar
                )
            }
        }
    }


    /**
     * The set of [names](https://en.wikipedia.org/wiki/Name) that something goes by.
     *
     * # ⚠️ WARNING ⚠️
     * [KnownAliases] is meant to be used only in the context of [AliasMatcher], and should be optimized with that in mind.
     * For example, while logically the [canonical] and [aliases] together could be considered a [Set],
     * we don't bother validating their uniqueness because doing so is only meaningful when we know [AliasMatcher.ignoreCase]
     * and the other entries in the [AliasMatcher.aliasMap].
     */
    @ConsistentCopyVisibility
    data class KnownAliases internal constructor(
        val canonical: String,
        val aliases: ImmutableList<String>,
    ) {
        @PublishedApi
        internal constructor(
            canonical: CharSequence,
            aliases: Iterable<String>, // 📎 Do not use `Iterable<CharSequence>`, even though it's tempting: doing so would force a copy of `aliases` into a new `ImmutableList<String>`, because we can't do an accurate type check for `is ImmutableList<String>`.
        ) : this(canonical.toString(), ImmutableList.copyOf(aliases))

        init {
            require(canonical.isNotBlank() && aliases.all { it.isNotBlank() }) { "None of the ${javaClass.simpleName} values can be blank: $this" }
        }

        /**
         * Finds the strongest [AliasMatcher.getMatchiness] of [nameOrAbbreviation] against my [canonical] and [aliases].
         */
        fun getMatchiness(
            nameOrAbbreviation: CharSequence,
            ignoreCase: Boolean,
        ): Matchiness? {
            return aliases.fold<CharSequence, Matchiness?>(
                getMatchiness(
                    nameOrAbbreviation,
                    canonical,
                    ignoreCase
                ),
            ) { matchinessSoFar, alias ->
                getStrongerMatchiness(
                    nameOrAbbreviation,
                    alias,
                    ignoreCase,
                    matchinessSoFar
                )
            }
        }
    }
}

private fun getStrongerMatchiness(
    nameOrAbbreviation: CharSequence,
    fullName: CharSequence,
    ignoreCase: Boolean,
    matchinessSoFar: Matchiness?,
): Matchiness? {
    return when (matchinessSoFar) {
        Matchiness.Exact   -> matchinessSoFar
        Matchiness.Partial -> when (nameOrAbbreviation.contentEquals(fullName, ignoreCase)) {
            true -> Matchiness.Exact
            else -> matchinessSoFar
        }

        null               -> getMatchiness(
            nameOrAbbreviation = nameOrAbbreviation,
            fullName = fullName,
            ignoreCase
        )
    }
}

private fun validate(
    allKnownAliases: Iterable<KnownAliases>,
    ignoreCase: Boolean,
) {
    val allNames = allKnownAliases.asSequence()
        .flatMap {
            sequence {
                yield(it.canonical)
                yieldAll(it.aliases)
            }
        }

    val soFar = HashSet<String>()

    for (name in allNames) {
        val added = soFar.add(
            when (ignoreCase) {
                true  -> name.lowercase()
                false -> name
            }
        )

        if (added == false) {
            throw IllegalArgumentException(formatDuplicateAliases(allKnownAliases, ignoreCase))
        }
    }
}

/**
 * ⚠️ This should only be run **after** you've found at least 1 duplicate!
 */
private fun formatDuplicateAliases(
    allKnownAliases: Iterable<KnownAliases>,
    ignoreCase: Boolean,
): String {
    val flattened = allKnownAliases
        .asSequence()
        .flatMap {
            sequence {
                yield(it.canonical to it)
                yieldAll(
                    it.aliases.asSequence()
                        .map { alias -> alias to it }
                )
            }
        }

    val grouped = flattened.groupBy {
        when (ignoreCase) {
            true  -> it.component1().lowercase()
            false -> it.component1()
        }
    }

    val duplicates = grouped.filterValues { it.size > 1 }

    require(duplicates.isNotEmpty()) { "You shouldn't have called this method unless you knew you had at least one duplicate!" }

    fun formatDuplicateGroup(
        alias: String,
        group: List<Pair<String, KnownAliases>>,
    ) = group.joinToString(
        prefix = " 🔑 ${alias}:\n",
        separator = "\n",
        transform = { (dupe, aliases) ->
            "   ↳ $dupe in $aliases"
        }
    )

    return duplicates.entries.joinToString(
        prefix = "There were duplicate aliases (with `ignoreCase` = $ignoreCase):\n",
        separator = "\n",
        transform = { (alias, group) -> formatDuplicateGroup(alias, group) }
    )
}