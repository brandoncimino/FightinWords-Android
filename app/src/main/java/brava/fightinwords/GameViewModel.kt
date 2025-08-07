package brava.fightinwords

import androidx.lifecycle.ViewModel
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.WordState
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.ui.GameScreenInteractions
import brava.fightinwords.ui.GameScreenState
import brava.fightinwords.ui.GameScreenState.InGame.Companion.getScreenState
import brava.fightinwords.ui.typesetter.SortButton
import brava.fightinwords.ui.typesetter.SortButton.Companion.clickSortButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private lateinit var gameInProgress: GameInProgress

    private val _gameScreenState = MutableStateFlow<GameScreenState>(GameScreenState.Loading)
    val gameScreenState: StateFlow<GameScreenState> = _gameScreenState.asStateFlow()

    fun clickLetterButton(index: Int) {
        gameInProgress.typesetter.toggleIndex(index)
        refresh()
    }

    fun clickSortButton(sortButton: SortButton) {
        gameInProgress.typesetter.clickSortButton(sortButton)
        refresh()
    }

    fun clickGalleyButton(galleyIndex: Int) {
        gameInProgress.typesetter.toggleGalleyIndex(galleyIndex)
        refresh()
    }

    fun clickSubmitButton(): Umpire.SubmissionResult {
        val result = gameInProgress.submitGalley()
        refresh()
        return result
    }

    fun clickFilterButton(
        wordFilter: WordFilter.State,
    ) {
        blog { "Clicking the filter button: $wordFilter" }
        gameInProgress.ledgerman.toggleWordFilter(wordFilter.wordFilter)
        refresh()
    }

    fun focusOnWord(wordState: WordState) {
        if (wordState is DefinedWordState) {
            gameInProgress.ledgerman.focusOnWord(wordState)
            refresh()
        }
    }

    fun expandFocusedWord() {
        gameInProgress.ledgerman.expandFocusedWord()
        refresh()
    }

    fun collapseFocusedWord() {
        gameInProgress.ledgerman.collapseFocusedWord()
        refresh()
    }

    private fun refresh() {
        _gameScreenState.update { gameInProgress.getScreenState() }
    }

    fun setGameInProgress(gameInProgress: GameInProgress) {
        this.gameInProgress = gameInProgress
        refresh()
    }

    val gameScreenInteractions = GameScreenInteractions(
        this::clickSortButton,
        this::clickLetterButton,
        this::clickGalleyButton,
        this::clickSubmitButton,
        this::clickFilterButton,
        this::focusOnWord,
        this::expandFocusedWord,
        this::collapseFocusedWord,
    )

    fun getSerializableState() = gameInProgress.getSerializableState()
}