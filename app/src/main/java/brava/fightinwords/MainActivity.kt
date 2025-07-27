package brava.fightinwords

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import brava.fightinwords.botlin.blog
import brava.fightinwords.botlin.putJson
import brava.fightinwords.botlin.readJson
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.WordFileLookup
import brava.fightinwords.gameplay.wordlookup.WordLookup
import brava.fightinwords.gameplay.wordlookup.WordLookupHelpers
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.GameUi
import brava.fightinwords.ui.theme.FightinWordsTheme
import brava.fightinwords.ui.typesetter.UiSettings
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    lateinit var gameUi: GameUi
    lateinit var gameInProgress: GameInProgress

    val naspaWordList by lazy { WordFileLookup(assets.open("en/NWL2023_words.txt")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveGameState = savedInstanceState?.loadSaveGameState()

        this.gameInProgress = when (saveGameState) {
            null -> startFreshGame(GamePlan(naspaWordList.randomWordLetterPool()))
            else -> GameInProgress.resumeGame(saveGameState)
        }

        gameUi = GameUi.create(gameInProgress, uiSettings = UiSettings())

        enableEdgeToEdge()
        setContent {
            FightinWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    GameScreen(
                        typesetterState = gameUi.typesetterUi.state,
                        typesetterButtons = gameUi.typesetterUi.buttons,
                        focusedDefinition = gameUi.focusedDefinitionUi.state,
                        modifier = Modifier.padding(it),
                        onExpandDefinition = gameUi.focusedDefinitionUi.onExpand,
                        onCollapseDefinition = gameUi.focusedDefinitionUi.onCollapse,
                        visibleWords = gameUi.umpireUi.visibleWords,
                        onWordClicked = { word -> if (word is DefinedWordState) gameUi.focusedDefinitionUi.onFocus(word) },
                        uiSettings = gameUi.uiSettings
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val gameState = gameInProgress.getSerializableState()
        outState.putJson(gameState)

        // The official example puts the call to the `super` method at the _end_ of the child: https://developer.android.com/guide/components/activities/activity-lifecycle#save-simple,-lightweight-ui-state-using-onsaveinstancestate
        super.onSaveInstanceState(outState)
    }

    private fun Bundle.loadSaveGameState(): SaveGameState? {
        blog { "We have a saved ${javaClass.simpleName}; attempting to load a ${SaveGameState::class.simpleName} from it..." }

        return runCatching { this.readJson<SaveGameState>() }
            .getOrElse {
                blog(Log.ERROR) { "Unable to load a valid ${SaveGameState::class.simpleName} from the savedInstanceState!" }
                return@getOrElse null
            }
    }

    private fun WordLookup.randomWordLetterPool(wordLength: Int = 6): LetterPool {
        return findRandomWord(wordLength, Random)
            .map { LetterPool(it) }
            .getOrThrow()
    }

    fun startFreshGame(
        gamePlan: GamePlan,
        definitionsCsvAssetPath: String = "en/definitions.csv",
        wordScorer: WordScorer = ScrabbleScorer
    ): GameInProgress {
        val gameInProgress = GameInProgress.startGame(
            gamePlan = gamePlan,
            wordPool = WordLookupHelpers.parseConstructibleWords(
                csvStream = assets.open(definitionsCsvAssetPath),
                language = KnownLanguage.English,
                letterPool = gamePlan.letterPool
            ),
            wordScorer = wordScorer
        )

        return gameInProgress
    }
}