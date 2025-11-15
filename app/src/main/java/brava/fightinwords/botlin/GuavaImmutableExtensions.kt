package brava.fightinwords.botlin

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet

/**
 * @return A new [ImmutableList] containing [this] and one additional [element].
 */
operator fun <T : Any> ImmutableList<T>.plus(element: T): ImmutableList<T> {
    val builder = ImmutableList.builderWithExpectedSize<T>(size + 1)
    builder.addAll(this)
    builder.add(element)
    return builder.build()
}

/**
 * @return An [ImmutableList] containing [this] followed by [elements].
 */
operator fun <T : Any> ImmutableList<T>.plus(elements: Iterable<T>): ImmutableList<T> {
    return when (elements) {
        is Collection -> {
            if (elements.isEmpty()) {
                return this
            }
            ImmutableList.builderWithExpectedSize<T>(size + elements.size)
        }

        else          -> ImmutableList.builder<T>()
    }
        .addAll(this)
        .addAll(elements)
        .build()
}

/**
 * @see ImmutableList.copyOf
 */
fun <T : Any> Sequence<T>.toImmutableList(): ImmutableList<T> {
    return ImmutableList.copyOf(iterator())
}

/**
 * @see ImmutableSet.copyOf
 */
fun <T : Any> Sequence<T>.toImmutableSet(): ImmutableSet<T> {
    return ImmutableSet.copyOf(iterator())
}