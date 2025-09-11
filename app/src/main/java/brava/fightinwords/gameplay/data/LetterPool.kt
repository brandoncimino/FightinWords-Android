package brava.fightinwords.gameplay.data

import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.botlin.TinyFlags
import com.google.common.primitives.ImmutableIntArray

/**
 * A [Word] optimized for [canConstruct].
 */
class LetterPool(
    val codePoints: ImmutableIntArray,
) {
    constructor(letters: Word) : this(letters.toCodePointArray())

    init {
        checkSize(codePoints.length())
    }

    val size inline get() = codePoints.length()

    fun canConstruct(word: Word): Boolean {
        if (word.length > codePoints.length()) {
            return false
        }

        return ListImplementation.containsAllElementsOf(
            codePoints.length(),
            codePoints::get,
            word.length,
            word::get,
            { a, b -> a == b }
        )
    }

    companion object {
        const val MAX_POOL_SIZE = TinyFlags.MAX_FLAG_COUNT
    }
}

private fun checkSize(size: Int) {
    // 📎 Not checking that `size` > 0 because that wouldn't offer much value in exchange for making unit tests and `@ComposablePreview`s more annoying.

    check(size < LetterPool.MAX_POOL_SIZE) {
        "$size exceeds the${LetterPool::class.simpleName}.MAX_POOL_SIZE of ${LetterPool.MAX_POOL_SIZE}."
    }
}

private fun Word.toCodePointArray(): ImmutableIntArray {
    checkSize(length)

    val builder = ImmutableIntArray.builder(length)
    for (i in indices) {
        builder.add(get(i).codePoint)
    }
    return builder.build().trimmed()
}