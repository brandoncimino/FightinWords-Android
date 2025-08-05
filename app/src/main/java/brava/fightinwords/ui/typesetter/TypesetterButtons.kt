package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.ui.typesetter.SortButton.Companion.clickSortButton

data class TypesetterButtons(
    val onSortButtonClick: (SortButton) -> Unit,
    val onLetterButtonClick: (index: Int) -> Unit,
    val onGalleyButtonClick: (index: Int) -> Unit,
    val onSubmit: () -> Unit,
) {
    fun then(action: () -> Unit): TypesetterButtons {
        return TypesetterButtons(
            onSortButtonClick = { onSortButtonClick(it); action() },
            onLetterButtonClick = { onLetterButtonClick(it); action() },
            onGalleyButtonClick = { onGalleyButtonClick(it); action() },
            onSubmit = { onSubmit(); action() }
        )
    }

    companion object {
        fun clickSortButton(sortButton: SortButton, typesetter: Typesetter) {
            typesetter.clickSortButton(sortButton)
        }
    }
}

fun Typesetter.buttons(
    onSubmit: () -> Unit
): TypesetterButtons {
    return TypesetterButtons(
        onSortButtonClick = { clickSortButton(it) },
        onLetterButtonClick = this::toggle,
        onGalleyButtonClick = { this.toggle(this.galley.get(it)) },
        onSubmit = onSubmit
    )
}