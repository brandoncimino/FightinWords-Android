package brava.fightinwords

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup
import brava.fightinwords.gameplay.wordlookup.DefinitionsCsvLookup
import brava.fightinwords.gameplay.wordlookup.NaspaWordList
import brava.fightinwords.gameplay.wordlookup.WordLookup
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.UiSettings
import brava.fightinwords.ui.theme.FightinWordsTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.cbor.Cbor
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()
    private val snackbarHostState = SnackbarHostState()

    /**
     * The format used to save/load the game state from a `savedInstanceState` [Bundle].
     */
    @OptIn(ExperimentalSerializationApi::class)
    private val serialFormat: SerialFormat = Cbor

    /**
     * Previous implementation, in case I need to go back:
     *
     * ```java
     *     val naspaWordList by lazy { WordFileLookup(assets.open("en/NWL2023_words.txt")) }
     * ```
     */
    val naspaWordList by lazy {
        return@lazy NaspaWordList(getCachedAssetBytes("en/NWL2023.txt"))
    }

    val definitionsCsvLookup by lazy {
        return@lazy DefinitionsCsvLookup(getCachedAssetBytes("en/definitions.csv"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveGameState = savedInstanceState?.loadSaveGameState()

        gameViewModel.setGameInProgress(
            when (saveGameState) {
                null -> startFreshGame(
                    GamePlan(naspaWordList.randomWordLetterPool()),
                    definitionsCsvLookup,
                    definitionsCsvLookup
                )
                else -> GameInProgress.resumeGame(saveGameState)
            }
        )



        enableEdgeToEdge()
        setContent {
            SubmissionSnackbar()

            FightinWordsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) {
                    GameScreen(
                        gameViewModel.gameScreenState.collectAsState().value,
                        gameViewModel.gameScreenInteractions,
                        uiSettings = UiSettings(),
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }

    @Composable
    private fun SubmissionSnackbar(enabled: Boolean = false) {
        if (!enabled) {
            return
        }

        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            gameViewModel.submissionResults.collect { submissionResult ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "${submissionResult.wordState.javaClass.simpleName}",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val gameState = gameViewModel.getSerializableState()
        outState.putSingleton(gameState, serialFormat)

        // The official example puts the call to the `super` method at the _end_ of the child: https://developer.android.com/guide/components/activities/activity-lifecycle#save-simple,-lightweight-ui-state-using-onsaveinstancestate
        super.onSaveInstanceState(outState)
    }

    private fun Bundle.loadSaveGameState(): SaveGameState? {
        blog { "We have a saved ${javaClass.simpleName}; attempting to load a ${SaveGameState::class.simpleName} from it..." }

        return runCatching { this.getSingleton<SaveGameState>(serialFormat) }
            .getOrElse {
                blog(Log.ERROR) {
                    """Unable to load a valid ${SaveGameState::class.simpleName} from the savedInstanceState due to:
```
${it.stackTraceToString()}
```"""
                }
                return@getOrElse null
            }
    }

    private fun WordLookup.randomWordLetterPool(wordLength: Int = 6): Word {
        return findRandomWord(wordLength, Random)
            .getOrThrow()
    }

    fun startFreshGame(
        gamePlan: GamePlan,
        wordLookup: WordLookup,
        definitionLookup: DefinitionLookup,
        wordScorer: WordScorer = ScrabbleScorer,
    ): GameInProgress {
        val letterPool = LetterPool(gamePlan.letterPool)

        val allAllPossibleWords = (gamePlan.minimumWordLength..gamePlan.letterPool.length)
            .asSequence()
            .flatMap {
                wordLookup.findAllPossibleWords(letterPool, it)
            }

        val gameInProgress = GameInProgress.startGame(
            gamePlan = gamePlan,
            wordPool = allAllPossibleWords,
            wordScorer = wordScorer,
            definitionLookup = definitionLookup
        )

        return gameInProgress
    }
}