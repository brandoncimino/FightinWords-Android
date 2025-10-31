package brava.fightinwords.botlin

/**
 * This is basically [androidx.compose.ui.util.fastForEach],
 * but I don't like having [android] or [androidx] dependencies floating around my code.
 */
private inline fun <T> Iterable<T>.smartForEach(
    action: (T) -> Unit,
) {
    when (this) {
        is List<T> -> {
            for (i in indices) {
                action(get(i))
            }
        }

        else       -> forEach(action)
    }
}

/**
 * Similar to [Iterable.contains], but using [CharSequence.contentEquals] to allow the specification of [ignoreCase].
 *
 * 📎 Though the generated Java code looks like it might prevent the [List]-specific optimizations in [smartForEach] from being utilized from here
 * due to explicit casts of [this] to [Iterable], that isn't the case.
 * In other words, there is no need to `inline` this.
 *
 * 📎 We could defer to the standard [Iterable.contains] method when [ignoreCase] is `false`.
 * However, we want to take advantage of [smartForEach]'s optimizations when possible.
 *
 * @return `true` if [element] is [CharSequence.contentEquals] to any of my elements
 */
fun Iterable<CharSequence>.contains(element: CharSequence, ignoreCase: Boolean): Boolean {
    if (ignoreCase == false && this is Set) {
        return this.contains(element)
    }

    smartForEach {
        if (it.contentEquals(element, ignoreCase)) {
            return true
        }
    }

    return false
}