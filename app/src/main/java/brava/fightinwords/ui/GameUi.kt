package brava.fightinwords.ui

import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.UmpireUi.Companion.createUi
import brava.fightinwords.ui.typesetter.TypesetterUi
import brava.fightinwords.ui.typesetter.TypesetterUi.Companion.createUi
import brava.fightinwords.ui.typesetter.UiSettings

class GameUi {
    val uiSettings: UiSettings
    val umpireUi: UmpireUi
    val typesetterUi: TypesetterUi

    constructor(
        progenitorPool: LetterPool,
        wordPool: Sequence<WordDefinition>,
        wordScorer: WordScorer = ScrabbleScorer(),
        uiSettings: UiSettings = UiSettings()
    ) {
        println("Chose a starting letter pool: $progenitorPool")

        this.uiSettings = uiSettings

        val umpire = Umpire(
            wordPool = wordPool,
            wordScorer = wordScorer
        )
        umpireUi = umpire.createUi()
        val typesetter = Typesetter(progenitorPool)
        typesetterUi = typesetter.createUi(
            processSubmittedWord = umpireUi::submitWord
        )
    }
}

