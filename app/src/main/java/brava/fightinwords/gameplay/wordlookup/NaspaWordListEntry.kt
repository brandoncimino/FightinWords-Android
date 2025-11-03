package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.AsciiBytes
import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.botlin.Substring.Companion.fastSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.botlin.indexOf
import brava.fightinwords.gameplay.data.TinyWord.Companion.toTinyWord
import brava.fightinwords.gameplay.wordlookup.AnnotatedDefinitionPart.Companion.appendInline
import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.Companion.linkEnd
import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.Companion.linkStart

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
 *
 * MISSING GLOSS?!
 * ```
 * DEFENESTRATE [v DEFENESTRATED, DEFENESTRATES, DEFENESTRATING] : DEFENESTRATION [n]
 * ```
 */
@ConsistentCopyVisibility
data class NaspaWordListEntry internal constructor(
    val rawEntry: AsciiBytes,
    val wordRange: TinyRange,
    val definitionRange: TinyRange,
    val partOfSpeechRange: TinyRange,
) {
    companion object {
        const val inlineStart = '<'
        const val inlineEnd = '>'

        const val linkStart = '{'
        const val linkEnd = '}'

        private const val space = ' '.code.toByte()
        private const val leftSquareBracket = '['.code.toByte()
        private const val rightSquareBracket = ']'.code.toByte()
        private const val equals = '='.code.toByte()

        fun parse(
            rawEntry: AsciiBytes,
        ): NaspaWordListEntry {
            val spaceAfterWord = rawEntry.bytes.indexOf({ it == space })
            val openingSquareBracket =
                rawEntry.bytes.indexOf({ it == leftSquareBracket }, startIndex = spaceAfterWord + 1)

            val partOfSpeechStartsAt = openingSquareBracket + 1
            val partOfSpeechEndsAt = rawEntry.bytes.indexOf(
                { it == rightSquareBracket || it == space },
                startIndex = partOfSpeechStartsAt
            )

            return NaspaWordListEntry(
                rawEntry = rawEntry,
                wordRange = 0 til spaceAfterWord,
                definitionRange = (spaceAfterWord + 1) til (openingSquareBracket - 1),
                partOfSpeechRange = (openingSquareBracket + 1) til partOfSpeechEndsAt
            )
        }

        fun parseAnnotatedParts(
            def: AsciiBytes,
            naspaWordList: NaspaWordList,
        ): List<AnnotatedDefinitionPart> {
            val parts = buildList {
                ListImplementation.forEachWrappedRange(
                    sourceStart = 0,
                    sourceEndInclusive = def.lastIndex,
                    isWrapperStart = {
                        val char = def[it]
                        char == linkStart || char == inlineStart
                    },
                    isWrapperEndInclusive = { rangeStart, rangeEndInclusive ->
                        val firstByte = def[rangeStart]
                        val lastByte = def[rangeEndInclusive]
                        when (firstByte) {
                            linkStart   -> lastByte == linkEnd
                            inlineStart -> lastByte == inlineEnd
                            else        -> throw IllegalStateException("This should have been impossible!")
                        }
                    },
                    unwrappedRangeAction = { rangeStart, rangeEndInclusive ->
                        add(
                            AnnotatedDefinitionPart.Literal(
                                def.slice(
                                    rangeStart,
                                    rangeEndInclusive
                                ).bytes
                            )
                        )
                    },
                    wrappedRangeAction = { start, endInclusive ->
                        val wordKey = parseNaspaWordKey(
                            def,
                            start,
                            endInclusive
                        )
                        when (def[start]) {
                            linkStart   -> {
                                add(
                                    AnnotatedDefinitionPart.Link(wordKey)
                                )
                            }

                            inlineStart -> {
                                appendInline(wordKey, naspaWordList)
                            }
                        }
                    },
                )
            }

            return parts
        }

        /**
         * Extracts a [WordKey] from a "reference" in a [NaspaWordListEntry.definitionRange],
         * e.g. `{bisexual=n}` in `BI a {bisexual=n} [n BIS]`.
         *
         * @param definition The full [NaspaWordListEntry.definitionRange]
         * @param startDelimiterIndex The index in the [definition] that indicated the beginning of the word key, e.g. [linkStart] in `{bisexual=n}`.
         * @param endDelimiterIndex The index in the [definition] of the closing character, e.g. [linkEnd] in `{bisexual=n}`.
         */
        fun parseNaspaWordKey(
            definition: CharSequence,
            startDelimiterIndex: Int,
            endDelimiterIndex: Int,
        ): WordKey {
            val splitterIndex = ListImplementation.indexOf(
                startDelimiterIndex,
                endDelimiterIndex,
                { definition[it] == '=' }
            )

            val wordSlice = definition.fastSlice(
                start = startDelimiterIndex + 1,
                endInclusive = splitterIndex - 1
            )
            val partOfSpeech =
                definition.fastSlice(
                    start = splitterIndex + 1,
                    endInclusive = endDelimiterIndex - 1
                )
            return WordKey(
                wordSlice.toTinyWord(),
                KnownPartOfSpeech.aliasMatcher.requireMatch(partOfSpeech)
            )
        }
    }
}