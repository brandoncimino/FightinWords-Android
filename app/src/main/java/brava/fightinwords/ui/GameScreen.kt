package brava.fightinwords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.*
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.submissions.DefinitionBox
import brava.fightinwords.ui.submissions.WordPoolView
import brava.fightinwords.ui.typesetter.*

//@Composable
//fun GameScreen(
//    gameScreenState: GameScreenState,
//    typesetterButtons: TypesetterButtons,
//    modifier: Modifier = Modifier,
//    onExpandDefinition : () -> Unit = {},
//    onCollapseDefinition : () -> Unit = {},
//    uiSettings: UiSettings
//){
//    val typesetterState = remember { derivedStateOf { gameScreenState.typesetterState } }.value
//    GameScreen(
//        typesetterState = typesetterState,
//        typesetterButtons = typesetterButtons,
//        focusedDefinition = remember { derivedStateOf { gameScreenState.focusedDefinition } }.value,
//        modifier = modifier,
//        onExpandDefinition = onExpandDefinition,
//        onCollapseDefinition = onCollapseDefinition,
//        visibleWords = remember {derivedStateOf { gameScreenState.visibleWordStates } }.value,
//        onWordClicked = {},
//        uiSettings = uiSettings
//    )
//}

@Composable
fun GameScreen(
    typesetterState: TypesetterState,
    typesetterButtons: TypesetterButtons,
    focusedDefinition: FocusLens.State<DefinedWordState>?,
    modifier: Modifier = Modifier,
    onExpandDefinition: () -> Unit = {},
    onCollapseDefinition: () -> Unit = {},
    visibleWords: List<WordState>,
    onWordClicked: (WordState) -> Unit,
    uiSettings: UiSettings = UiSettings()
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        for (section in uiSettings.sectionOrder) {
            when (section) {
                UiSettings.UiSection.Scoreboard -> {
                    WordPoolView(
                        visibleWords,
                        modifier = Modifier.weight(1f, fill = false)
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        onWordClicked = onWordClicked
                    )
                }

                UiSettings.UiSection.Definition -> {
                    DefinitionBox(
                        focusedDefinition?.target,
                        modifier = Modifier
                            .height(height = 150.dp)
                            .fillMaxWidth()
                            .padding(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ),
                        onClick = onExpandDefinition
                    )
                }

                UiSettings.UiSection.Typesetter -> {
                    TypesetterView(
                        typesetterState,
                        typesetterButtons,
                        uiSettings,
                    )
                }
            }
        }

        // The definition popup
        blog { "checking for the definition popup: zoomed = ${focusedDefinition?.zoomed}" }
        if (focusedDefinition?.zoomed == true) {
            Dialog(
                onDismissRequest = onCollapseDefinition
            ) {
                DefinitionBox(
                    focusedDefinition.target,
                    onClick = onCollapseDefinition
                )
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

    GameScreen(
        typesetterState = typesetter.snapshot(),
        typesetterButtons = typesetter.buttons {},
        focusedDefinition = FocusLens.State(
            Unplayed(deez)
        ),
        visibleWords = listOf(
            Unplayed(deez.copy(word = "aaaa".toWord(), isNaspaWord = true)),
            Accepted(deez.copy(word = "bbbb".toWord(), isNaspaWord = true), 99),
            Unplayed(deez.copy(word = "cccc".toWord(), isNaspaWord = false)),
            Accepted(deez.copy(word = "dddd".toWord(), isNaspaWord = false), 999),
        ),
        onWordClicked = {}
    )
}