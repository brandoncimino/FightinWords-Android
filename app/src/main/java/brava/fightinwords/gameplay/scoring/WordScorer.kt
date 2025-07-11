package brava.fightinwords.gameplay.scoring

import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Word

fun interface WordScorer {
    fun getScore(word: Word, language: KnownLanguage): Int
}