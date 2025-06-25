package brava.fightinwords.gameplay.wordlookup

import java.io.InputStream

class WordFileLookup(wordStream: InputStream) : WordLookup {
    private val words: Set<String> = wordStream.bufferedReader(Charsets.UTF_8)
        .useLines { lines -> lines.toSet() }

    override fun isWord(word: String): Boolean = words.contains(word)
}