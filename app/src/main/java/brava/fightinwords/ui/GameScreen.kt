package brava.fightinwords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.submissions.DefinitionBox
import brava.fightinwords.ui.submissions.WordPoolView
import brava.fightinwords.ui.typesetter.*


@Composable
fun GameScreen(
    typesetterState: TypesetterState,
    typesetterButtons: TypesetterButtons,
    focusedDefinition: FocusedDefinitionState?,
    onExpandDefinition: () -> Unit = {},
    onCollapseDefinition: () -> Unit = {},
    visibleWords: List<DefinedWordState>,
    onWordClicked: (DefinedWordState) -> Unit,
    uiSettings: UiSettings,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
//        TopBar(
//            modifier = Modifier.height(100.dp)
//                .background(Color.LightGray, MaterialTheme.shapes.large)
//        )

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
                        focusedDefinition?.definedWord,
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
        if (focusedDefinition?.poppedUp == true) {
            Dialog(
                onDismissRequest = onCollapseDefinition
            ) {
                DefinitionBox(
                    focusedDefinition.definedWord,
                    onClick = onCollapseDefinition
                )
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "(this space intentionally left blank)",
            fontStyle = FontStyle.Italic
        )
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
        typesetterButtons = typesetter.buttons({}),
        focusedDefinition = FocusedDefinitionState(
            Unplayed(deez)
        ),
        visibleWords = listOf(
            Unplayed(deez.copy(word = "aaaa".toWord(), isNaspaWord = true)),
            Accepted(deez.copy(word = "bbbb".toWord(), isNaspaWord = true), 99),
            Unplayed(deez.copy(word = "cccc".toWord(), isNaspaWord = false)),
            Accepted(deez.copy(word = "dddd".toWord(), isNaspaWord = false), 999),
        ),
        uiSettings = UiSettings(),
        onWordClicked = {}
    )
}