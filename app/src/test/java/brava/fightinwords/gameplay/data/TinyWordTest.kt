package brava.fightinwords.gameplay.data

import brava.fightinwords.gameplay.data.Word.Companion.toWord
import com.google.common.collect.ArrayListMultimap
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.streams.asSequence

class TinyWordTest {
    @Test
    fun noCollisionsTest() {
//        assertNoCollisions { TinyWord.packAZ(it) }
//        val count = allStrings(('a'..'z').toList(), 3..7).count()
//        println("count = ${count}")
        val word = "aaa"
        println("word = ${word}")
        val hash = TinyWord.packAZ(word)
        println("hash = ${hash}")

        println("hash and 0b111 = ${hash and 0b111}")
        println("98304 and 0b111 = ${98304 and 0b111}")
        val anded = 98304 and 0b111

        val unhashed = TinyWord.unpackAZ(hash)
        println("unhashed = ${unhashed}")

        assert(word == unhashed)

        assertNoCollisions(
            hashFunction = { TinyWord.packAZ(it) },
            unhashFunction = { TinyWord.unpackAZ(it) }
        )

    }

    @Test
    fun noCollisionsPolyHash() {
//        assertNoCollisions { TinyWord.polyHash(it) }
    }

    fun assertNoCollisions(
        lengths: IntRange = 0..12,
        hashFunction: (String) -> Long,
        unhashFunction: (Long) -> CharSequence,
    ) {
        val words = readWordFile(lengths)

//        val hashes = HashMap<Long, String>()
        val hashes = ArrayListMultimap.create<Long, String>()

        var isSorted = true

        var printCount = 0
        var previousHash = -1L
        var previousWord: String? = null
        for (word in words) {
            if (printCount++ < 10) {
                println("word #$printCount = [${word.length}]${word}")
                println("first char = ${word[0]} // ${word[0].category} // ${Character.getName(word[0].code)} // ${word[0].isWhitespace()}")
                val trimmed = word.trim()
                println("trimmed = [${trimmed.length}]$trimmed")
            }
            val hash = hashFunction(word)
            val existingWord = hashes.put(hash, word)
//            assert(existingWord == null, {"The word $word produced the hash $hash, which was already held by $existingWord! (successes: ${hashes.count()-1})"})
            val reversed = unhashFunction(hash)
            Assertions.assertThat(reversed)
                .isEqualTo(word)

            if (hash <= previousHash) {
                isSorted = false
                println("Word `$word` has the hash `$hash`, which is less than the previous word, `$previousWord` ($previousHash)")
            }

            previousHash = hash
            previousWord = word
        }

        val collisions = hashes.asMap().filter { it -> it.value.size > 1 }
        Assertions.assertThat(collisions).isEmpty()
        Assertions.assertThat(isSorted).isTrue
    }

    private fun readWordFile(lengths: IntRange): Stream<String> =
        Files.lines(Path.of(wordFilePath))
            .map { word ->
                word.trimStart { c ->
                    if (!c.isLetter()) {
                        println("word `$word` started with the non-letter `$c` '${Character.getName(c.code)}")
                    }
                    return@trimStart !c.isLetter()
                }
            }
            .filter { it.length in lengths }

    val tinyWordFilePath: String = """C:\Users\brand\IdeaProjects\FightinWords\app\src\main\assets\en\tinywords.txt"""
    val wordFilePath: String = """C:\Users\brand\IdeaProjects\FightinWords\app\src\main\assets\en\words.txt"""

    @Test
    fun makeTinyWords() {
        TinyWord.writePackedWordFile(
            readWordFile(1..12).asSequence(),
            tinyWordFilePath
        )

        val tinyWordCount = TinyWord.readPackedWordFile(tinyWordFilePath, { words -> words.count() })
        println("tinyWordCount = ${tinyWordCount}")
    }

    @Test
    fun findPlayableWords() {
        val letterPool = LetterPool("obtuse".toWord())

        val comp: Comparator<String> = Comparator.comparing { it.length }

        val wordPool = readWordFile(4..letterPool.size)
            .filter { letterPool.canConstruct(it.toWord()) }
            .asSequence()
            .sortedWith(comp.thenBy { it })
            .toList()

        println("wordPool.size = ${wordPool.size}")
        println("4+: ${wordPool.count { it.length >= 4 }}")

        wordPool.forEach { println("\"$it\",") }
    }

    @Test
    fun listPatternTest() {
        val parts = listOf(1, 2, 3)
        val (a, b) = parts
        println("a = ${a}")
        println("b = ${b}")
//        println("c = ${c}")
//        println("d = ${d}")

    }

    @Test
    fun safeDelimiterTest() {
        val hasPipe = readWordFile(0..Int.MAX_VALUE)
            .anyMatch { it.contains('|') }
        println("hasPipe = ${hasPipe}")
    }
}