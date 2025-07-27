package brava.fightinwords

import android.util.Log
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.*
import brava.fightinwords.gameplay.data.Word
import kotlinx.serialization.Serializable

@Serializable
data class SaveGameState(
    val gamePlan: GamePlan,
    val slugs: List<Slug.State>,
    val wordStates: List<WordState>,
    val focusedWordState: FocusLens.State<Word>?,
) {
    companion object {
        fun SaveGameState.getFocusedWordDefinition(): FocusLens.State<DefinedWordState>? {
            return focusedWordState?.let { (word, zoomed) ->
                val wordState = wordStates.find { it.word == word }
                when (wordState) {
                    is DefinedWordState -> return FocusLens.State(wordState, zoomed)
                    else -> {
                        blog(Log.ERROR) {
                            "Tried to load the focusedDefinition from the ${javaClass.simpleName}, but it wasn't a ${DefinedWordState::class.simpleName} - it was ${wordState?.javaClass?.simpleName ?: "null"}!"
                        }
                        return null
                    }
                }
            }
        }
    }
}