package brava.fightinwords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Typesetter
import brava.fightinwords.gameplay.Umpire
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.gameplay.data.LetterPool
import brava.fightinwords.ui.submissions.DefinitionBox
import brava.fightinwords.ui.submissions.WordPoolView
import brava.fightinwords.ui.typesetter.*

@Composable
fun GameScreen(
    typesetter: Typesetter,
    umpire: Umpire,
    uiSettings: UiSettings,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        typesetterState = typesetter.snapshot(),
        typesetterButtons = typesetter.buttons(),
        focusedDefinition = null,
        playableWords = umpire.snapshot().flatMap {
            when (it) {
                is DefinedWordState -> sequenceOf(it)
                else -> emptySequence()
            }
        },
        uiSettings = uiSettings,
        modifier = modifier,
    )
}

@Composable
fun GameScreen(
    typesetterState: TypesetterState,
    typesetterButtons: TypesetterButtons,
    focusedDefinition: DefinedWordState?,
    playableWords: List<DefinedWordState>,
    uiSettings: UiSettings,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        TopBar(
            modifier = Modifier.height(100.dp)
                .background(Color.LightGray, MaterialTheme.shapes.large)
        )

        WordPoolView(
            playableWords,
            modifier = Modifier.weight(1f, fill = false)
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        )

        DefinitionBox(
            focusedDefinition,
            modifier = Modifier
                .sizeIn(minHeight = 120.dp)
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
        )

        TypesetterView(
            typesetterState,
            typesetterButtons,
            uiSettings,
        )
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
        typesetterButtons = typesetter.buttons(),
        focusedDefinition = Unplayed(PreviewHelpers.deez),
        playableWords = obtuseSubmissions(),
        uiSettings = UiSettings()
    )
}