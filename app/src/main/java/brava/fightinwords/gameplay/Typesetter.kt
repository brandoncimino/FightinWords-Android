package brava.fightinwords.gameplay

import brava.fightinwords.Letter
import brava.fightinwords.gameplay.Phonology.Companion.englishPhonology
import kotlin.random.Random

class Typesetter(
    /**
     * The original [Slug]s that the game was started with.
     */
    val progenitorPool: List<Char>
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
    val galley: Galley = Galley(progenitorPool.size)

    var currentSorting: LetterSorting? = null;

    fun shuffle(random: Random) {
        currentPool = currentPool.shuffled(random)
        currentSorting = null;
    }

    fun sortPhonologically() {
        currentPool = currentPool.sortedBy { it.letter.englishPhonology }
        currentSorting = LetterSorting.Phonological
    }

    fun sortAlphabetically() {
        currentPool = currentPool.sortedBy { it.letter }
        currentSorting = LetterSorting.Alphabetical
    }

    fun select(slug: Slug) {
        return galley.add(slug);
    }

    fun deselect(slug: Slug) {
        return galley.remove(slug);
    }

    fun clear() {
        galley.submitAndClear()
    }

    fun submitAndClear(): List<Letter> {
        return galley.submitAndClear()
    }

    fun Slug.isSlotted() {
        galley.contains(this)
    }
}