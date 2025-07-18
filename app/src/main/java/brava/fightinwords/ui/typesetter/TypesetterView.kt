package brava.fightinwords.ui.typesetter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.*
import brava.fightinwords.ui.swaggins

@Composable
fun TypesetterView(
    typesetterState: TypesetterState,
    typesetterButtons: TypesetterButtons,
    uiSettings: UiSettings,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val actualPersonalSpace: Dp
        val comfortablePersonalSpace = 360.dp / UiSettings.ComfortableButtonsPerRow

        if (uiSettings.maxButtonsPerRow == null) {
            actualPersonalSpace = comfortablePersonalSpace
        } else {
            val minPersonalSpace = this.maxWidth / uiSettings.maxButtonsPerRow
            actualPersonalSpace = min(comfortablePersonalSpace, minPersonalSpace)
        }
        Column(horizontalAlignment = CenterHorizontally) {
            GalleyView(
                typesetterState.galley,
                uiSettings,
                actualPersonalSpace,
                typesetterButtons.onGalleyButtonClick
            )

            LetterPoolView(
                typesetterState.letterButtonStates,
                uiSettings,
                personalSpace = actualPersonalSpace,
                typesetterButtons.onLetterButtonClick
            )

            SortButtons(
                typesetterButtons.onSortButtonClick,
                typesetterButtons.onSubmit
            )
        }
    }
}

@Composable
@Preview(device = Devices.PIXEL, showSystemUi = true)
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
fun TypesetterViewPreview(
    @PreviewParameter(UiSettingsPreviewProvider::class) uiSettings: UiSettings
) {
    TypesetterView(
        swaggins(),
        TypesetterButtons({}, {}, {}, {}),
        uiSettings
    )
}

@Composable
fun SortButtons(
    onSortButtonClick: (SortButton) -> Unit,
    onSubmit: () -> Unit,
    fontSize: TextUnit = 30.sp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                content = { Text("🎲", fontSize = fontSize) },
                onClick = { onSortButtonClick(SortButton.Shuffled) },
                modifier = modifier
            )

            Button(
                content = { Text("abc", fontSize = fontSize) },
                onClick = { onSortButtonClick(SortButton.Alphabetical) },
                modifier = Modifier
            )

            Button(
                content = { Text("aeiou", fontSize = fontSize) },
                onClick = { onSortButtonClick(SortButton.Phonological) },
                modifier = Modifier
            )

            Button(
                content = { Text("⏎", fontSize = fontSize) },
                onClick = onSubmit,
                modifier = Modifier
            )
        }
    }
}