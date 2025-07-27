package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.LetterSorting
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.ui.typesetter.TypesetterButtons.Companion.clickSortButton
import kotlin.random.Random

class TypesetterButtons(
    val onSortButtonClick: (SortButton) -> Unit,
    val onLetterButtonClick: (index: Int) -> Unit,
    val onGalleyButtonClick: (index: Int) -> Unit,
    val onSubmit: () -> Unit
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
            when (sortButton) {
                SortButton.Shuffled -> typesetter.shuffle(Random)
                SortButton.Alphabetical -> typesetter.requestLetterSorting(LetterSorting.Alphabetical)
                SortButton.Phonological -> typesetter.requestLetterSorting(LetterSorting.Phonological)
            }
        }
    }
}

fun Typesetter.buttons(
    onSubmit: () -> Unit
): TypesetterButtons {
    return TypesetterButtons(
        onSortButtonClick = { sortButton: SortButton -> clickSortButton(sortButton, this) },
        onLetterButtonClick = this::toggle,
        onGalleyButtonClick = { this.toggle(this.galley.get(it)) },
        onSubmit = onSubmit
    )
}