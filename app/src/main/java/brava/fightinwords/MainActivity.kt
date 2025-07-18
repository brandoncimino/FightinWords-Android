package brava.fightinwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.wordlookup.WordFileLookup
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.theme.FightinWordsTheme
import brava.fightinwords.ui.typesetter.UiSettings
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val naspaWordList = WordFileLookup(assets.open("en/NWL2023_words.txt"))

        val progenitorPool = naspaWordList.findRandomWord(7, Random)
            .map { LetterPool(it) }
            .getOrThrow()

        val umpire = Umpire(
            wordPool = WordLookupHelpers.parseConstructibleWords(
                assets.open("en/definitions.csv"),
                KnownLanguage.English,
                progenitorPool
            ),
            wordScorer = ScrabbleScorer()
        )

        enableEdgeToEdge()
        setContent {
            FightinWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    GameScreen(
                        typesetter = Typesetter(progenitorPool),
                        umpire = umpire,
                        uiSettings = UiSettings(),
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }
}