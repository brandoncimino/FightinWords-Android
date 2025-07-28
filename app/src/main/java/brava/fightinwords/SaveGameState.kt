package brava.fightinwords

import brava.fightinwords.gameplay.GamePlan
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.scoring.Scoreboard
import kotlinx.serialization.Serializable

@Serializable
data class SaveGameState(
    val gamePlan: GamePlan,
    val typesetterState: Typesetter.SerializableState,
    val umpireState: Umpire.SerializableState,
    val scoreboardState: Scoreboard.SerializableState,
) {
}