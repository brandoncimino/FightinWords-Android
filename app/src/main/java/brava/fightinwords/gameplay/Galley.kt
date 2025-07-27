package brava.fightinwords.gameplay

import android.util.Log
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.toWord

class Galley<T>(
    val capacity: Int,
    initialState: Iterable<T> = listOf()
) {
    private val composingStick: ArrayDeque<T> = ArrayDeque<T>(capacity)
        .apply { addAll(initialState) }

    val isFull : Boolean get() {
        // 📎 You can do this in an "expression" style, using:
        //        isFull : Boolean get() = value;
        //    Equivalent to C#'s:
        //        boolean isFull => value;
        //    But that's confusing, because it LOOKS, but is NOT the same, as C#'s:
        //        boolean isFull = value;
        return composingStick.size == capacity
    }

    fun add(slug: T, log: (String) -> Unit = {}) {
        val beforeSize = composingStick.size
        if(isFull) {
            throw IllegalStateException("Can't add $slug because I am already at my full capacity of ${capacity}!")
        }

        if(composingStick.contains(slug)) {
            throw IllegalArgumentException("I already contain $slug!")
        }

        composingStick.add(slug);

        log("Adding $slug produced: $composingStick")

        assert(composingStick.size > beforeSize)
    }

    operator fun get(index: Int): T {
        return composingStick[index]
    }

    fun remove(slug: T) {
        if(composingStick.contains(slug) == false) {
            throw NoSuchElementException("I don't contain $slug!")
        }

        composingStick.remove(slug)
        blog(Log.VERBOSE) { "Removing $slug produced: $composingStick" }
    }

    fun clear(){
        composingStick.clear()
    }

    fun backspace(): T? {
        return composingStick.removeLastOrNull()
    }

    fun contains(slug: T): Boolean {
        return composingStick.contains(slug)
    }

    fun elementAtOrNull(index: Int): T? {
        return composingStick.elementAtOrNull(index)
    }

    fun snapshot(): List<T> {
        return composingStick.toList()
    }

    companion object {
        fun Galley<Slug>.currentLetters(): Word {
            return composingStick.map { it.letter }.toWord()
        }
    }

    fun indexOf(element: T): Int {
        return composingStick.indexOf(element)
    }
}