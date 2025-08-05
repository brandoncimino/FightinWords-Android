package brava.fightinwords.ui

import brava.fightinwords.gameplay.WordState
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.ui.typesetter.SortButton
import brava.fightinwords.ui.typesetter.TypesetterButtons

data class GameScreenInteractions(
    val onSortButtonClick: (SortButton) -> Unit,
    val onLetterButtonClick: (index: Int) -> Unit,
    val onGalleyButtonClick: (index: Int) -> Unit,
    val onSubmit: () -> Unit,
    val onFilterButtonClick: (WordFilter.State) -> Unit,
    val onFocusWord: (WordState) -> Unit,
    val onExpandFocusedWord: () -> Unit,
    val onCollapseFocusedWord: () -> Unit,
) {
    val typesetterButtons = TypesetterButtons(
        onSortButtonClick,
        onLetterButtonClick,
        onGalleyButtonClick,
        onSubmit,
    )
}