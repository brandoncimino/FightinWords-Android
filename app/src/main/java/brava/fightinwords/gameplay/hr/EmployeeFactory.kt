package brava.fightinwords.gameplay.hr

import brava.fightinwords.SaveGameState
import brava.fightinwords.gameplay.GamePlan

interface EmployeeFactory<
        EMPLOYEE,
        SERIALIZABLE_STATE
        > {
    fun EMPLOYEE.getSerializableState(): SERIALIZABLE_STATE

    fun fromSerializableState(state: SERIALIZABLE_STATE, gamePlan: GamePlan): EMPLOYEE

    fun SaveGameState.getEmployeeState(): SERIALIZABLE_STATE;

    companion object {
        fun <E, S> EmployeeFactory<E, S>.load(saveGameState: SaveGameState): E {
            return fromSerializableState(saveGameState.getEmployeeState(), saveGameState.gamePlan)
        }
    }
}