package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.LetterSorting
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.data.Word
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
                SortButton.Alphabetical -> requestLetterSorting(LetterSorting.Alphabetical, typesetter)
                SortButton.Phonological -> requestLetterSorting(LetterSorting.Phonological, typesetter)
            }
        }

        private fun requestLetterSorting(letterSorting: LetterSorting, typesetter: Typesetter) {
            val currentSorting = typesetter.currentSorting;
            val descending = when (currentSorting?.letterSorting) {
                letterSorting -> !currentSorting.isDescending
                else -> false
            }

            return typesetter.sort(letterSorting, descending)
        }
    }
}

fun Typesetter.buttons(
    processSubmittedWord: (Word) -> Unit
): TypesetterButtons {
    return TypesetterButtons(
        onSortButtonClick = { sortButton: SortButton -> clickSortButton(sortButton, this) },
        onLetterButtonClick = this::toggle,
        onGalleyButtonClick = { this.toggle(this.galley.get(it)) },
        onSubmit = {
            val submittedWord = this.submitAndClear()
            processSubmittedWord(submittedWord)
        }
    )
}