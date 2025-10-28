package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.ByteSlice
import brava.fightinwords.botlin.TinyRange.Companion.endInclusive
import brava.fightinwords.botlin.TinyRange.Companion.isEmpty
import brava.fightinwords.botlin.fastSlice
import brava.fightinwords.botlin.getMemoryMappedBuffer
import brava.fightinwords.botlin.sequenceOfNotNull
import brava.fightinwords.botlin.toUtf8String
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.TinyWord
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.wordlookup.NaspaWordListEntry.Companion.parseAnnotatedParts
import com.google.common.base.Stopwatch
import java.io.File

fun NaspaWordList(
    file: File,
): NaspaWordList {
    val stopwatch = Stopwatch.createStarted()
    val nwl = NaspaWordList(file.getMemoryMappedBuffer().fastSlice())
    val elapsed = stopwatch.elapsed()
    println("Loaded $file in $elapsed")
    return nwl
}

fun NaspaWordList(
    bytes: ByteSlice,
): NaspaWordList {
    val index = parseWordLineRanges(
        bytes,
    )

    return NaspaWordList(index, bytes)
}

class NaspaWordList(
    val index: ShortlexWordListIndex,
    private val bytes: ByteSlice,
) : DefinitionLookup, ShortlexWordList {
    override val id: WordList.Id get() = WordSource.NaspaWordList2023

    override fun getCountOfWordsWithLength(wordLength: Int): Int =
        index.wordLengthCounts[wordLength]

    override val wordLengthRange: IntRange get() = index.wordLengthRange

    override val wordCount: Int = index.wordCount

    override fun getWordByIndex(wordIndex: Int): Word {
        return index.getWordByIndex(wordIndex)
    }

    fun findEntry(word: Word): NaspaWordListEntry? {
        return when (word) {
            is TinyWord -> findEntry(word)
            else        -> null
        }
    }

    fun findRawEntry(word: TinyWord): ByteSlice? {
        val range = index.findWordRange(word)
        return when {
            range.isEmpty -> null
            else          -> bytes.slice(range)
        }
    }

    fun findEntry(word: TinyWord): NaspaWordListEntry? {
        return findRawEntry(word)?.let {
            NaspaWordListEntry.parse(it)
        }
    }

    override fun findDefinition(word: Word): WordDefinition? {
        return findEntry(word)?.toWordDefinition()
    }

    override fun findAllDefinitions(word: Word): Sequence<WordDefinition> {
        return sequenceOfNotNull(findDefinition(word))
    }

    override fun isWord(word: Word): Boolean {
        return when (word) {
            is TinyWord -> isWord(word)
            else        -> false
        }
    }

    fun isWord(word: TinyWord): Boolean {
        return index.findWordIndex(word) >= 0
    }

    fun entries(): Sequence<NaspaWordListEntry> {
        return (0..wordCount).asSequence()
            .map {
                val range = index.getRangeByIndex(it)
                val rawEntry = bytes.slice(range)
                return@map NaspaWordListEntry.parse(rawEntry)
            }
    }

    fun NaspaWordListEntry.toWordDefinition(): WordDefinition {
        val definitionSlice = rawEntry.slice(definitionRange)
        return WordDefinition(
            word = TinyWord.of(rawEntry, wordRange.start, wordRange.endInclusive),
            language = KnownLanguage.English,
            partOfSpeech =
                rawEntry.slice(partOfSpeechRange).toUtf8String(),
            definition = definitionSlice.toUtf8String(),
            source = WordSource.NaspaWordList2023,
            annotatedParts = parseAnnotatedParts(definitionSlice, this@NaspaWordList)
        )
    }
}

private fun parseWordLineRanges(
    bytes: ByteSlice,
): ShortlexWordListIndex {
    return ShortlexWordListIndex.build(bytes) { lineStart, lineEndInclusive ->
        TinyWord.extractTinyWordFromRange(
            ' '.code.toByte(),
            lineStart,
            lineEndInclusive,
            bytes::get,
            TinyWord.Companion.LongWordHandling.Skip
        )
    }
}