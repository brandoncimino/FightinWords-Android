package brava.fightinwords.ui

import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.GameInProgress
import brava.fightinwords.ui.FocusLensUi.Companion.createUi
import brava.fightinwords.ui.UmpireUi.Companion.createUi
import brava.fightinwords.ui.typesetter.TypesetterUi
import brava.fightinwords.ui.typesetter.TypesetterUi.Companion.createUi
import brava.fightinwords.ui.typesetter.UiSettings

class GameUi(
    val umpireUi: UmpireUi,
    val typesetterUi: TypesetterUi,
    val focusedDefinitionUi: FocusLensUi<DefinedWordState>,
    val uiSettings: UiSettings,
) {
    companion object {
        fun create(
            gameInProgress: GameInProgress,
            uiSettings: UiSettings
        ): GameUi {
            val umpireUi = gameInProgress.umpire.createUi(gameInProgress.gamePlan.unsubmittedWordVisibility)
            val focusLensUi = gameInProgress.focusedDefinition.createUi()
            val typesetterUi = gameInProgress.typesetter.createUi(
                onSubmitWord = {
                    gameInProgress.submitGalley()
                    umpireUi.refresh()
                    focusLensUi.refresh()
                }
            )

            return GameUi(
                umpireUi,
                typesetterUi,
                focusLensUi,
                uiSettings
            )
        }
    }
}

