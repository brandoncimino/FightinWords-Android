package brava.fightinwords.gameplay.wordlookup

interface WordLookup {
    abstract fun isWord(word: String): Boolean;
}