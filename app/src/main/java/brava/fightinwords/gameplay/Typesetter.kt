package brava.fightinwords.gameplay

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.Galley.Companion.currentLetters
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.indices
import brava.fightinwords.gameplay.hr.EmployeeFactory
import kotlinx.serialization.Serializable
import java.util.Comparator.comparing
import kotlin.random.Random

class Typesetter(
    slugs: List<Slug>,
    val galley: Galley<Slug> = Galley(slugs.size)
) {
    /**
     * @param progenitorPool The original [Slug]s that the game was started with.
     */
    constructor(progenitorPool: Iterable<Letter>) : this(
        progenitorPool.map { Slug(it) }
    )

    constructor(progenitorPool: Word) : this(
        buildList<Slug> {
            for (i in progenitorPool.indices) {
                add(Slug(progenitorPool[i]))
            }
        }
    )

    /**
     * The selectable letters that are being played with, in the order that they are visible to the player.
     */
    var currentPool: List<Slug> = slugs
        private set

    /**
     * Constructs a [Typesetter] that is already "in-progress".
     */
    constructor(
        slugStates: Collection<Slug.State>
    ) : this(
        slugStates.map { Slug(it.letter) },
    ) {
        val indexed = slugStates.withIndex()
        indexed.filter<IndexedValue<Slug.State>> { (_, state) -> state.galleyIndex >= 0 }
            .sortedBy<IndexedValue<Slug.State>, Int> { (_, state) -> state.galleyIndex }
            .forEach<IndexedValue<Slug.State>> { (index, _) -> galley.add(currentPool[index]) }
    }

    /**
     * The selected letters waiting to be submitted.
     */
//    val galley: Galley<Slug> = Galley(slugs.size)

    private data class SortState(val letterSorting: LetterSorting, val isDescending: Boolean)

    private var currentSorting: SortState? = null;

    fun shuffle(random: Random) {
        currentPool = currentPool.shuffled(random)
        currentSorting = null;
    }

    fun sort(letterSorting: LetterSorting, descending: Boolean = false) {
        val sortState = SortState(letterSorting, descending)
        currentPool = currentPool.sortedWith(
            comparing(
                { it.letter },
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

    fun toggleGalleyIndex(galleyIndex: Int) {
        toggle(galley[galleyIndex])
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

    fun requestLetterSorting(letterSorting: LetterSorting) {
        val currentSorting = currentSorting;
        val descending = when (currentSorting?.letterSorting) {
            letterSorting -> !currentSorting.isDescending
            else -> false
        }

        return sort(letterSorting, descending)
    }

    @JvmInline
    @Serializable
    value class SerializableState(val slugStates: List<Slug.State>)

    companion object : EmployeeFactory<Typesetter, SerializableState> {
        override fun Typesetter.getSerializableState(): SerializableState {
            return currentPool.map {
                Slug.State(
                    it.letter,
                    galley.indexOf(it)
                )
            }.let { SerializableState(it) }
        }

        override fun fromSerializableState(
            state: SerializableState,
            sharedResources: EmployeeFactory.SharedResources,
        ): Typesetter {
            return Typesetter(state.slugStates)
        }

        override fun SaveGameState.getEmployeeState(): SerializableState = typesetterState
    }
}