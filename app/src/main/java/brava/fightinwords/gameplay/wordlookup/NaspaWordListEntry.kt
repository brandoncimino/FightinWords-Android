package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.ListImplementation
import brava.fightinwords.botlin.Substring.Companion.fastSlice
import brava.fightinwords.botlin.TinyRange
import brava.fightinwords.botlin.TinyRange.Companion.til
import brava.fightinwords.botlin.indexOf
import brava.fightinwords.gameplay.data.TinyWord
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
 */
@ConsistentCopyVisibility
data class NaspaWordListEntry internal constructor(
    val rawEntry: ByteSlice,
    val wordRange: TinyRange,
    val definitionRange: TinyRange,
    val partOfSpeechRange: TinyRange,
) {
    companion object {
        const val inlineStart = '<'.code.toByte()
        const val inlineEnd = '>'.code.toByte()

        const val linkStart = '{'.code.toByte()
        const val linkEnd = '}'.code.toByte()

        private const val space = ' '.code.toByte()
        private const val leftSquareBracket = '['.code.toByte()
        private const val rightSquareBracket = ']'.code.toByte()
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

        fun parseAnnotatedParts(
            def: ByteSlice,
            naspaWordList: NaspaWordList,
        ): List<AnnotatedDefinitionPart> {
            val parts = buildList {
                ListImplementation.forEachWrappedRange(
                    sourceStart = 0,
                    sourceEndInclusive = def.lastIndex,
                    isWrapperStart = {
                        val byte = def[it]
                        byte == linkStart || byte == inlineStart
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
                                )
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
         * Extracts a [WordKey] from a "reference" in a [brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.definitionRange],
         * e.g. `{bisexual=n}` in `BI a {bisexual=n} [n BIS]`.
         *
         * @param definition The full [brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.definitionRange]
         * @param wrapperStart The index in the [definition] that indicated the beginning of the word key, e.g. [linkStart] in `{bisexual=n}`.
         * @param wrapperEndInclusive The index in the [definition] of the closing character, e.g. [linkEnd] in `{bisexual=n}`.
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
}