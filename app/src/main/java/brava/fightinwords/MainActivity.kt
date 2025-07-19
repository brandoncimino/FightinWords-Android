package brava.fightinwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.wordlookup.WordFileLookup
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.theme.FightinWordsTheme
import brava.fightinwords.ui.typesetter.TypesetterUi.Companion.createUi
import brava.fightinwords.ui.typesetter.UiSettings
import java.util.concurrent.atomic.LongAdder
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    val counter = Counter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val naspaWordList = WordFileLookup(assets.open("en/NWL2023_words.txt"))

        val progenitorPool = naspaWordList.findRandomWord(7, Random)
            .map { LetterPool(it) }
            .getOrThrow()

        println("Chose a starting letter pool: $progenitorPool")

        val umpire = Umpire(
            wordPool = WordLookupHelpers.parseConstructibleWords(
                assets.open("en/definitions.csv"),
                KnownLanguage.English,
                progenitorPool
            ),
            wordScorer = ScrabbleScorer()
        )
        val typesetter = Typesetter(progenitorPool)
        val typesetterUi = typesetter.createUi()
        val uiSettings = UiSettings()


        enableEdgeToEdge()
        setContent {
            FightinWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    MutableScreen(counter, modifier = Modifier.padding(it))
                    GameScreen(
                        typesetterState = typesetterUi.state,
                        typesetterButtons = typesetterUi.buttons,
                        focusedDefinition = null,
                        playableWords = umpire.snapshot().filterIsInstance<DefinedWordState>(),
                        uiSettings = uiSettings,
                        modifier = Modifier.padding(it),
                    )
                }
            }
        }
    }
}

class Counter {
    private val adder: LongAdder = LongAdder();

    val longValue: Long
        get() = adder.toLong()

    fun increment(): Long {
        adder.increment()
        return adder.toLong()
    }

    override fun toString(): String {
        return adder.toString()
    }
}

@Composable
fun MutableScreen(counter: Counter, modifier: Modifier) {
    var count by remember { mutableLongStateOf(counter.longValue) }
    Column(modifier) {
        Text("Click count: $counter // $count")
        Button(onClick = {
            println("Before click: $counter // $count")
            count = counter.increment()
            println("After click: $counter // $count")
        }) {
            Text("Increment")
        }
    }
}