package brava.fightinwords.ui.typesetter

import brava.fightinwords.gameplay.Galley.Companion.currentLetters
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.data.Word

data class TypesetterState(
    val galley: Word,
    val letterButtonStates: List<LetterButtonState>
)

fun Typesetter.snapshot(): TypesetterState {
    return TypesetterState(
        this.galley.currentLetters(),
        this.currentPool.map {
            LetterButtonState(it.letter, it.isSlotted())
        }
    )
}