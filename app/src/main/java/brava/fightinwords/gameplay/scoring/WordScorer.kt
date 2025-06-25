package brava.fightinwords.gameplay.scoring

import brava.fightinwords.gameplay.KnownLanguage

interface WordScorer {
    fun getScore(word: String, language: KnownLanguage): Int
}