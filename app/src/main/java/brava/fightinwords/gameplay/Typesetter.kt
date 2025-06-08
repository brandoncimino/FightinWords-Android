package brava.fightinwords.gameplay

import brava.fightinwords.Letter
import kotlin.random.Random

class Typesetter(
    /**
     * The original [Slug]s that the game was started with.
     */
    val progenitorPool: List<Slug>
) {
    /**
     * The selectable letters that are being played with, in the order that they are visible to the player.
     */
    var currentPool: List<Slug> = progenitorPool
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
        currentPool = progenitorPool.sortedBy { it.letter.phonology }
        currentSorting = LetterSorting.Phonological
    }

    fun sortAlphabetically() {
        currentPool = progenitorPool.sortedBy { it.letter }
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
}