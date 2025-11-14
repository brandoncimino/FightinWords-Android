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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import brava.fightinwords.botlin.AsciiBytes.Companion.toAsciiUnsafe
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.scoring.ScrabbleScorer
import brava.fightinwords.gameplay.scoring.WordScorer
import brava.fightinwords.gameplay.scoring.WordScoringStrategy
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup
import brava.fightinwords.gameplay.wordlookup.DefinitionsCsvLookup
import brava.fightinwords.gameplay.wordlookup.NaspaWordList
import brava.fightinwords.gameplay.wordlookup.WordList
import brava.fightinwords.gameplay.wordlookup.WordLookup
import brava.fightinwords.gameplay.wordlookup.WordSource
import brava.fightinwords.gameplay.wordlookup.WordSourceLoader
import brava.fightinwords.gameplay.wordlookup.getRandomWord
import brava.fightinwords.ui.GameScreen
import brava.fightinwords.ui.UiSettings
import brava.fightinwords.ui.theme.FightinWordsTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.cbor.Cbor

const val DefaultLetterPoolSize = 6
val DefaultCoreWordList: WordList.Id = WordSource.NaspaWordList2023

class MainActivity : ComponentActivity(), WordSourceLoader {
    private val gameViewModel: GameViewModel by viewModels()

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
    private val naspaWordList by lazy {
        return@lazy NaspaWordList(
            getCachedAssetBytes("en/NWL2023.txt").toAsciiUnsafe()
        )
    }

    private val definitionsCsvLookup by lazy {
        return@lazy DefinitionsCsvLookup(getCachedAssetBytes("en/definitions.csv"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveGameState = savedInstanceState?.loadSaveGameState()

        gameViewModel.setGameInProgress(
            when (saveGameState) {
                null -> {
                    val letterPool = getWordList(DefaultCoreWordList)
                        .getRandomWord(DefaultLetterPoolSize)
                    val gamePlan = GamePlan(letterPool)
                    startFreshGame(
                        gamePlan = gamePlan
                    )
                }

                else -> GameInProgress.resumeGame(this, saveGameState)
            }
        )

        enableEdgeToEdge()
        setContent {
            FightinWordsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
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

    fun startFreshGame(
        gamePlan: GamePlan,
    ): GameInProgress {
        return GameInProgress.startGame(this, gamePlan)
    }

    override fun getWordLookup(id: WordLookup.Id): WordLookup {
        return when (id) {
            WordSource.DefinitionsCsv    -> definitionsCsvLookup
            WordSource.NaspaWordList2023 -> naspaWordList
            WordSource.WiktionaryHttpApi -> TODO()
        }
    }

    override fun getDefinitionLookup(id: DefinitionLookup.Id): DefinitionLookup {
        return when (id) {
            WordSource.DefinitionsCsv    -> definitionsCsvLookup
            WordSource.NaspaWordList2023 -> naspaWordList
            WordSource.WiktionaryHttpApi -> TODO()
        }
    }

    override fun getWordList(id: WordList.Id): WordList {
        return when (id) {
            is WordSource.NaspaWordList2023 -> naspaWordList
            WordSource.DefinitionsCsv       -> definitionsCsvLookup
        }
    }

    override fun getWordScorer(strategy: WordScoringStrategy): WordScorer {
        return when (strategy) {
            WordScoringStrategy.Scrabble -> ScrabbleScorer as WordScorer
        }
    }

    override val allDefinitionLookups: List<DefinitionLookup> by lazy {
        listOf(naspaWordList, definitionsCsvLookup)
    }
}