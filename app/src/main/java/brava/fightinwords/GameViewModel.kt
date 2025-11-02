package brava.fightinwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.gameplay.SubmissionResult
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.gameplay.wordlookup.WordKey
import brava.fightinwords.ui.GameScreenInteractions
import brava.fightinwords.ui.GameScreenState
import brava.fightinwords.ui.GameScreenState.InGame.Companion.getScreenState
import brava.fightinwords.ui.typesetter.SortButton
import brava.fightinwords.ui.typesetter.SortButton.Companion.clickSortButton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private lateinit var gameInProgress: GameInProgress
    fun getGameInProgress() = gameInProgress

    private val _gameScreenState = MutableStateFlow<GameScreenState>(GameScreenState.Loading)
    val gameScreenState: StateFlow<GameScreenState> = _gameScreenState.asStateFlow()

    private val _submissionResults = MutableSharedFlow<SubmissionResult>()
    val submissionResults: SharedFlow<SubmissionResult> = _submissionResults.asSharedFlow()

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

    fun clickSubmitButton(): SubmissionResult {
        val result = gameInProgress.submitGalley()
        emitSubmissionResult(result)
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

    fun focusOnWord(wordKey: WordKey) {
        gameInProgress.ledgerman.focusOnWord(wordKey)
        refresh()
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

    private fun emitSubmissionResult(submissionResponse: SubmissionResult) {
        viewModelScope.launch {
            _submissionResults.emit(submissionResponse)
        }
    }
}