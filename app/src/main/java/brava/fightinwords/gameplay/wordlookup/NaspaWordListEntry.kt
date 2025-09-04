package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.botlin.indexOf
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.length
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.gameplay.data.Word
import java.nio.ByteBuffer

@ConsistentCopyVisibility
data class NaspaWordListEntry internal constructor(
    private val rawEntry: ByteBuffer,
    private val wordRange: TinyRange,
    private val definitionRange: TinyRange,
    private val partOfSpeechRange: TinyRange,
) {
    val word get() = rawEntry[wordRange]
    val definition get() = rawEntry[definitionRange]
    val partOfSpeech get() = rawEntry[partOfSpeechRange]

    companion object {
        private const val space = ' '.code.toByte()
        private const val leftSquareBracket = '['.code.toByte()
        private const val rightSquareBracket = ']'.code.toByte()

        fun parse(rawEntry: ByteBuffer) : NaspaWordListEntry {
            val spaceAfterWord = rawEntry.indexOf({it == space})
            val openingSquareBracket = rawEntry.indexOf({it == leftSquareBracket}, startIndex = spaceAfterWord + 1)

            val partOfSpeechStartsAt = openingSquareBracket + 1
            val partOfSpeechEndsAt = rawEntry.indexOf({ it == rightSquareBracket || it == space }, startIndex = partOfSpeechStartsAt)

            return NaspaWordListEntry(
                rawEntry = rawEntry,
                wordRange = 0 til spaceAfterWord,
                definitionRange = (spaceAfterWord + 1) til (openingSquareBracket - 1),
                partOfSpeechRange = (openingSquareBracket + 1) til partOfSpeechEndsAt
            )
        }

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        private operator fun ByteBuffer.get(range: TinyRange): ByteBuffer = slice(range.start, range.length)
    }
}

data class NaspaWordKey(
    val word: Word,
    val partOfSpeech: String,
)

sealed interface NaspaWordReference {
    val range: IntRange
    val word: NaspaWordKey

    data class Inline(
        override val range: IntRange,
        override val word: NaspaWordKey,
    ) : NaspaWordReference

    data class Link(
        override val range: IntRange,
        override val word: NaspaWordKey,
    ) : NaspaWordReference
}