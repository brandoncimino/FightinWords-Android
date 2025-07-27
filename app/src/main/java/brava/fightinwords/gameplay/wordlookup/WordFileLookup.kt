package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import java.io.InputStream
import kotlin.random.Random

class WordFileLookup(wordStream: InputStream) : WordLookup {
    private val words: HashSet<Word> = HashSet()

    val size: Int = words.size

    private val wordLengthCounts: HashMap<Int, Int> = HashMap()

    init {
        wordStream.bufferedReader().forEachLine {
            assert(it.isNotEmpty())

            words.add(it.trim { c -> c.isLetter() == false }.toWord())
            // 📎 `?:` is equivalent to C#'s `??`
            wordLengthCounts.compute(it.length) { _, count -> (count ?: 0) + 1 }
        }

        blog { "Loaded ${words.size} words!" }
    }

    override fun isWord(word: Word): Boolean = words.contains(word)

    override fun findRandomWord(desiredLength: Int, random: Random): Result<Word> {
        val wordsOfThatLength = wordLengthCounts.getOrDefault(desiredLength, 0)

        if (wordsOfThatLength < 0) {
            return Result.failure(IllegalStateException("I don't know any words that are $desiredLength letters long!"))
        }

        val wordIndex = random.nextInt(wordsOfThatLength)
        return words.stream()
            .filter { it.size == desiredLength }
            .skip(wordIndex.toLong())
            .findAny()
            .map { Result.success(it) }
            .orElseThrow { NoSuchElementException("This shouldn't have been possible - it means I messed up, and couldn't find the ${wordIndex}-th word of length $desiredLength") }
    }

    override fun findAllPossibleWords(letterPool: LetterPool, wordLength: Int): List<Word> {
        val buffer = CharArray(letterPool.size)
        return words.filter { word ->
            letterPool.canConstructInternal(word.asSequence().map { it.character }, buffer)
        }
    }
}