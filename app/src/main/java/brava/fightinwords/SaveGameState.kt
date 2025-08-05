package brava.fightinwords

import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.scoring.Ledgerman
import kotlinx.serialization.Serializable

@Serializable
data class SaveGameState(
    val gamePlan: GamePlan,
    val typesetterState: Typesetter.SerializableState,
    val ledgermanState: Ledgerman.State,
) {
}