package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.Substring.Companion.get
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.gameplay.data.Word

@ConsistentCopyVisibility
data class NaspaWordListEntry internal constructor(
    private val rawEntry: String,
    private val wordRange: TinyRange,
    private val definitionRange: TinyRange,
    private val partOfSpeechRange: TinyRange,
) {
    val word get() = rawEntry[wordRange]
    val definition get() = rawEntry[definitionRange]
    val partOfSpeech get() = rawEntry[partOfSpeechRange]

    companion object {
        private var partOfSpeechEndingMarkers = charArrayOf(']', ' ')

        fun parse(rawEntry: String): NaspaWordListEntry {
            val spaceAfterWord = rawEntry.indexOf(' ')
            val openingSquareBracket = rawEntry.indexOf('[', startIndex = spaceAfterWord + 1)

            val partOfSpeechStartsAt = openingSquareBracket + 1
            val partOfSpeechEndsAt = rawEntry.indexOfAny(partOfSpeechEndingMarkers, startIndex = partOfSpeechStartsAt)

            return NaspaWordListEntry(
                rawEntry = rawEntry,
                wordRange = 0 til spaceAfterWord,
                definitionRange = (spaceAfterWord + 1) til (openingSquareBracket - 1),
                partOfSpeechRange = (openingSquareBracket + 1) til partOfSpeechEndsAt
            )

        }
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