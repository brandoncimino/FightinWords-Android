package brava.fightinwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.wordlookup.WordFileLookup
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.GameUi
import brava.fightinwords.ui.theme.FightinWordsTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    lateinit var gameUi: GameUi

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val naspaWordList = WordFileLookup(assets.open("en/NWL2023_words.txt"))

        val progenitorPool = naspaWordList.findRandomWord(7, Random)
            .map { it.shuffled() }
            .map { LetterPool(it) }
            .getOrThrow()

        println("Chose a starting letter pool: $progenitorPool")

        gameUi = GameUi(
            progenitorPool = progenitorPool,
            wordPool = WordLookupHelpers.parseConstructibleWords(
                assets.open("en/definitions.csv"),
                KnownLanguage.English,
                progenitorPool
            )
        )

        enableEdgeToEdge()
        setContent {
            FightinWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    GameScreen(
                        typesetterState = gameUi.typesetterUi.state,
                        typesetterButtons = gameUi.typesetterUi.buttons,
                        focusedDefinition = gameUi.umpireUi.focusedDefinition,
                        onExpandDefinition = gameUi.umpireUi::expandDefinition,
                        onCollapseDefinition = gameUi.umpireUi::collapseDefinition,
                        visibleWords = gameUi.umpireUi.visibleWords,
                        uiSettings = gameUi.uiSettings,
                        modifier = Modifier.padding(it),
                        onWordClicked = gameUi.umpireUi::focusDefinition
                    )
                }
            }
        }
    }
}