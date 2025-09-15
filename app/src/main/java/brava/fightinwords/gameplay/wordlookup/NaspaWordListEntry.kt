package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.botlin.Substring.Companion.fastSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.botlin.forEachWrappedRange
import brava.fightinwords.botlin.indexOf
import brava.fightinwords.botlin.toUtf8String
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.Companion.leftSquiggly
import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.Companion.rightSquiggly

/**
 * A single line of the [NASPA Word List](https://www.scrabbleplayers.org/w/NASPA_Word_List).
 *
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
    val rawEntry: ByteSlice,
    val wordRange: TinyRange,
    val definitionRange: TinyRange,
    val partOfSpeechRange: TinyRange,
) {
    fun definitionSlice() = rawEntry.slice(definitionRange)

    companion object {
        const val inlineStart = '<'.code.toByte()
        const val inlineEnd = '>'.code.toByte()

        const val linkStart = '{'.code.toByte()
        const val linkEnd = '}'.code.toByte()

        private const val space = ' '.code.toByte()
        private const val leftSquareBracket = '['.code.toByte()
        private const val rightSquareBracket = ']'.code.toByte()
        private const val lessThan = '<'.code.toByte()
        private const val greaterThan = '>'.code.toByte()
        private const val leftSquiggly = '{'.code.toByte()
        private const val rightSquiggly = '}'.code.toByte()
        private const val equals = '='.code.toByte()

        fun parse(
            rawEntry: ByteSlice,
        ): NaspaWordListEntry {
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

        fun parseSubstitutions(definition: ByteSlice): List<WordDefinitionSubstitution> {
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

        /**
         * Extracts a [WordKey] from a "reference" in a [brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.definition],
         * e.g. `{bisexual=n}` in `BI a {bisexual=n} [n BIS]`.
         *
         * @param definition The full [brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.definition]
         * @param wrapperStart The index in the [definition] that indicated the beginning of the word key, e.g. [leftSquiggly] in `{bisexual=n}`.
         * @param wrapperEndInclusive The index in the [definition] of the closing character, e.g. [rightSquiggly] in `{bisexual=n}`.
         */
        fun parseNaspaWordKey(
            definition: ByteSlice,
            wrapperStart: Int,
            wrapperEndInclusive: Int,
        ) : WordKey {
            val delimiterIndex =
                definition.indexOf(equals, wrapperStart + 1, wrapperEndInclusive - 1)

            return WordKey(
                TinyWord.of(
                    definition,
                    wrapperStart + 1,
                    delimiterIndex - 1
                ),
                TinyWord.of(
                    definition,
                    delimiterIndex + 1,
                    wrapperEndInclusive - 1
                )
            )
        }

        fun parseNaspaWordKey(
            definition: CharSequence,
            startIndex: Int,
            endInclusive: Int,
        ): WordKey {
            val delimiterIndex = ListImplementation.indexOf(
                startIndex,
                endInclusive,
                { definition[it] == '=' }
            )
            val wordLength = delimiterIndex - startIndex
            val wordSlice = definition.fastSlice(startIndex, wordLength)
            val partOfSpeech =
                definition.fastSlice(delimiterIndex + 1, definition.length - wordLength - 1)
            return WordKey(
                TinyWord.of(wordSlice),
                TinyWord.of(partOfSpeech)
            )
        }
    }

    fun toWordDefinition(): WordDefinition {
        val definitionSlice = rawEntry.slice(definitionRange)
        return WordDefinition(
            word = TinyWord.of(rawEntry, wordRange.start, wordRange.endInclusive),
            language = KnownLanguage.English,
            partOfSpeech =
                rawEntry.slice(partOfSpeechRange).toUtf8String(),
            definition = definitionSlice.toUtf8String(),
            isNaspaWord = true,
            substitutions = parseSubstitutions(definitionSlice)
        )
    }
}