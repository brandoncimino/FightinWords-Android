package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.Galley.Companion.currentLetters
import brava.fightinwords.gameplay.Slug
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

@Serializable
data class TypesetterState(
    val galley: Word,
    val letterButtonStates: List<Slug.State>
)

fun Typesetter.snapshot(): TypesetterState {
    return TypesetterState(
        this.galley.currentLetters(),
        this.getSerializableState()
    )
}