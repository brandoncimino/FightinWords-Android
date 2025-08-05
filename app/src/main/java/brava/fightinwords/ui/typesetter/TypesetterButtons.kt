package brava.fightinwords.ui.typesetter

data class TypesetterButtons(
    val onSortButtonClick: (SortButton) -> Unit,
    val onLetterButtonClick: (index: Int) -> Unit,
    val onGalleyButtonClick: (index: Int) -> Unit,
    val onSubmit: () -> Unit,
)