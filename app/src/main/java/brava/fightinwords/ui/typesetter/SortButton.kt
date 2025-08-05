package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.LetterSorting
import brava.fightinwords.gameplay.Typesetter
import kotlin.random.Random

enum class SortButton(
    val label: String
) {
    Shuffled("🎲"),
    Alphabetical("A-Z"),
    Phonological("Vowels");

    companion object {
        fun Typesetter.clickSortButton(sortButton: SortButton) {
            when (sortButton) {
                SortButton.Shuffled     -> this.shuffle(Random)
                SortButton.Alphabetical -> this.requestLetterSorting(LetterSorting.Alphabetical)
                SortButton.Phonological -> this.requestLetterSorting(LetterSorting.Phonological)
            }
        }
    }
}