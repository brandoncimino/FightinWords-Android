package brava.fightinwords.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import brava.fightinwords.gameplay.*
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.submissions.DefinitionBox
import brava.fightinwords.ui.submissions.LedgermanWordPool
import brava.fightinwords.ui.submissions.ScoreboardWordFilters
import brava.fightinwords.ui.typesetter.TypesetterView
import brava.fightinwords.ui.typesetter.snapshot

@Composable
fun GameScreen(
    gameScreenState: GameScreenState,
    gameScreenInteractions: GameScreenInteractions,
    modifier: Modifier = Modifier,
    uiSettings: UiSettings = UiSettings(),
) {
    when (gameScreenState) {
        is GameScreenState.Loading -> Text("Loading...")
        is GameScreenState.InGame  -> {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.fillMaxSize()
            ) {
                for (section in uiSettings.sectionOrder) {
                    when (section) {
                        UiSettings.UiSection.Scoreboard    -> {
                            LedgermanWordPool(
                                modifier = Modifier.weight(1f, fill = true),
                                visibleWords = gameScreenState.ledgermanState.visibleWords,
                                onFocusWord = gameScreenInteractions.onFocusWord,
                            )
                        }

                        UiSettings.UiSection.Definition    -> {
                            DefinitionBox(
                                gameScreenState.ledgermanState.focusedWord?.target,
                                modifier = Modifier
                                    .height(height = 150.dp)
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                onClick = gameScreenInteractions.onExpandFocusedWord
                            )
                        }

                        UiSettings.UiSection.Typesetter    -> {
                            TypesetterView(
                                uiSettings = uiSettings,
                                typesetterState = gameScreenState.typesetterState,
                                typesetterButtons = gameScreenInteractions.typesetterButtons
                            )
                        }

                        UiSettings.UiSection.FilterButtons -> {
                            ScoreboardWordFilters(
                                wordFilterStates = gameScreenState.ledgermanState.wordFilterStates,
                                onWordFilterClicked = gameScreenInteractions.onFilterButtonClick,
                            )
                        }
                    }
                }

                // The definition popup
                val focusLensState = gameScreenState.ledgermanState.focusedWord
                if (focusLensState?.zoomed == true) {
                    Dialog(
                        onDismissRequest = gameScreenInteractions.onCollapseFocusedWord
                    ) {
                        DefinitionBox(
                            focusLensState.target,
                            onClick = gameScreenInteractions.onCollapseFocusedWord
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(device = Devices.PIXEL, showSystemUi = true)
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
fun GameScreenPreview() {
    val typesetter = Typesetter(LetterPool("obtuse".asSequence().map { Letter(it) }.toList()))
    typesetter.currentPool.forEachIndexed { index, slug ->
        if (index % 3 == 0) {
            typesetter.toggle(slug)
        }
    }

    val visibleWords = listOf(
        Unplayed(deez.copy(word = "aaaa".toWord(), isNaspaWord = true)),
        Accepted(deez.copy(word = "bbbb".toWord(), isNaspaWord = true), 99),
        Unplayed(deez.copy(word = "cccc".toWord(), isNaspaWord = false)),
        Accepted(deez.copy(word = "dddd".toWord(), isNaspaWord = false), 999),
    )

    val wordFilterStates = PreviewHelpers.wordFilterStates()

    val focusedWord = FocusLens.State<DefinedWordState>(Accepted(PreviewHelpers.deez, 99))
    val ledgermanUiState = LedgermanUiState(
        wordFilterStates,
        visibleWords,
        focusedWord
    )

    val uiSettings = UiSettings(
//        sectionOrder = listOf(
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//            UiSettings.UiSection.Definition,
//        )
    )

    GameScreen(
        gameScreenState = GameScreenState.InGame(
            typesetterState = typesetter.snapshot(),
            ledgermanState = ledgermanUiState,
        ),
        gameScreenInteractions = PreviewHelpers.gameScreenInteractions
    )

//    GameScreen(
//        GameUi(
//            typesetter.createUi(uiSettings),
//            LedgermanUi(
//                FocusLensUi(
//                    { focusedWord },
//                    uiSettings
//                ),
//                uiSettings,
//                {ledgermanUiState}
//            ),
//            uiSettings
//        )
//    )
}