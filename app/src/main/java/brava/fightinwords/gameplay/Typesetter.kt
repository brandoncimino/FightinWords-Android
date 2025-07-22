package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.Galley.Companion.currentLetters
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word
import java.util.Comparator.comparing
import kotlin.random.Random

class Typesetter(
    /**
     * The original [Slug]s that the game was started with.
     */
    val progenitorPool: LetterPool
) {
    /**
     * The selectable letters that are being played with, in the order that they are visible to the player.
     */
    var currentPool: List<Slug> = progenitorPool
        .map { Slug(it, this) }
        private set

    /**
     * The selected letters waiting to be submitted.
     */
    val galley: Galley<Slug> = Galley<Slug>(progenitorPool.size)

    data class SortState(val letterSorting: LetterSorting, val isDescending: Boolean)

    var currentSorting: SortState? = null;

    fun shuffle(random: Random) {
        currentPool = currentPool.shuffled(random)
        currentSorting = null;
    }

    fun sort(letterSorting: LetterSorting, descending: Boolean = false) {
        val sortState = SortState(letterSorting, descending)
        currentPool = currentPool.sortedWith(
            comparing(
                { it.letter.character },
                when (sortState.isDescending) {
                    true -> sortState.letterSorting.reversed()
                    false -> sortState.letterSorting
                }
            )
        )
        currentSorting = sortState
    }

    fun toggle(slug: Slug) {
        if (galley.contains(slug)) {
            deselect(slug)
            assert(galley.contains(slug) == false)
        } else {
            select(slug)
            assert(galley.contains(slug))
        }
    }

    fun toggleIndex(slugIndex: Int) = toggle(slugIndex)
    fun toggle(slugIndex: Int) {
        toggle(currentPool[slugIndex])
    }

    fun select(slug: Slug) {
        return galley.add(slug);
    }

    fun deselect(slug: Slug) {
        return galley.remove(slug);
    }

    fun submitAndClear(): Word {
        val word = galley.currentLetters()
        galley.clear()
        return word
    }

    fun Slug.isSlotted(): Boolean {
        return galley.contains(this)
    }
}