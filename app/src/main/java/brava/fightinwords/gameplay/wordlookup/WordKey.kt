package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.gameplay.data.Word

data class WordKey(
    val word: Word,
    val partOfSpeech: PartOfSpeech? = null,
)
