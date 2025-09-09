package brava.fightinwords.gameplay.wordlookup

import android.os.Build
import androidx.annotation.RequiresApi
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.length
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.botlin.forEachWrappedRange
import brava.fightinwords.botlin.indexOf
import brava.fightinwords.botlin.toUft8String
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import java.nio.ByteBuffer

/**
 * Suspicious entry:
 * ```
 * DID < DO, to begin and carry through to completion [v]
 * DOS < DO, the first tone of the diatonic musical scale [n]
 * ```
 * Is `< DO` an interpolation that should be replaced with `past tense of {do:v}`?
 * But then, what's the deal with `DOS < DO`?
 */
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
        private const val lessThan = '<'.code.toByte()
        private const val greaterThan = '>'.code.toByte()
        private const val leftSquiggly = '{'.code.toByte()
        private const val rightSquiggly = '}'.code.toByte()
        private const val equals = '='.code.toByte()

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

        fun parseSubstitutions(definition: ByteBuffer) : List<WordDefinitionSubstitution> {
            return buildList {
                definition.forEachWrappedRange(
                    leftSquiggly,
                    rightSquiggly
                ){
                    start, endInclusive ->
                    add(
                        WordDefinitionSubstitution.Link(
                        start..endInclusive,
                        parseNaspaWordKey(definition, start, endInclusive)
                    ))
                }

                definition.forEachWrappedRange(
                    lessThan,
                    greaterThan
                ) {
                    start, endInclusive ->
                    add(
                        WordDefinitionSubstitution.Inline(
                            start..endInclusive,
                            parseNaspaWordKey(definition, start, endInclusive)
                        )
                    )
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        fun parseNaspaWordKey(
            definition: ByteBuffer,
            startIndex: Int,
            endInclusive: Int
        ) : WordKey {
            val delimiterIndex = definition.indexOf(equals, startIndex, endInclusive)
            val wordLength = delimiterIndex - startIndex
            val wordSlice = definition.slice(startIndex, wordLength)
            val partOfSpeech = definition.slice(delimiterIndex+1, definition.limit() - wordLength)
            return WordKey(
                TinyWord.of(wordSlice),
                TinyWord.of(partOfSpeech)
            )
        }
    }

    fun toWordDefinition(): WordDefinition {
        return WordDefinition(
            word = TinyWord.of(word),
            language = KnownLanguage.English,
            partOfSpeech = partOfSpeech.toUft8String(),
            definition = definition.toUft8String(),
            isNaspaWord = true,
            substitutions = parseSubstitutions(definition)
        )
    }
}