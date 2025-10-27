package brava.fightinwords.gameplay.hr

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.WordPool
import brava.fightinwords.gameplay.wordlookup.Factotum
import brava.fightinwords.gameplay.wordlookup.Factotum.Companion.getFactotum
import brava.fightinwords.gameplay.wordlookup.WordSourceLoader

interface EmployeeFactory<
        EMPLOYEE,
        SERIALIZABLE_STATE
        > {
    fun EMPLOYEE.getSerializableState(): SERIALIZABLE_STATE

    fun fromSerializableState(state: SERIALIZABLE_STATE, sharedResources: SharedResources): EMPLOYEE

    fun SaveGameState.getEmployeeState(): SERIALIZABLE_STATE;

    companion object {
        fun <E, S> EmployeeFactory<E, S>.load(
            saveGameState: SaveGameState,
            sharedResources: SharedResources,
        ): E {
            return fromSerializableState(saveGameState.getEmployeeState(), sharedResources)
        }
    }

    data class SharedResources(
        private val wordSourceLoader: WordSourceLoader,
        val gamePlan: GamePlan,
    ) {
        val letterPool: LetterPool = LetterPool(gamePlan.letterPool)

        val factotum: Factotum = wordSourceLoader.getFactotum(gamePlan)

        val coreWordPool: WordPool = WordPool(
            letterPool,
            factotum.coreWordList,
            gamePlan.minimumWordLength
        )
    }
}