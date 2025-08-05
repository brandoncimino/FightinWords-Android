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
import brava.fightinwords.ui.UiSettings
import brava.fightinwords.ui.UiSettingsPreviewProvider

@Composable
fun TypesetterView(
    modifier: Modifier = Modifier,
    uiSettings: UiSettings,
    typesetterState: TypesetterState,
    typesetterButtons: TypesetterButtons,
) {
    BoxWithConstraints(modifier = modifier) {
        val actualPersonalSpace: Dp
        val comfortablePersonalSpace = 360.dp / uiSettings.comfortableButtonsPerRow

        if (uiSettings.maxButtonsPerRow == null) {
            actualPersonalSpace = comfortablePersonalSpace
        } else {
            val minPersonalSpace = this.maxWidth / uiSettings.maxButtonsPerRow
            actualPersonalSpace = min(comfortablePersonalSpace, minPersonalSpace)
        }
        Column(horizontalAlignment = CenterHorizontally) {
            GalleyView(
                typesetterState.galley,
                uiSettings = uiSettings,
                letterTilePersonalSpace = actualPersonalSpace,
                onClick = typesetterButtons.onGalleyButtonClick
            )

            LetterPoolView(
                typesetterState.letterButtonStates,
                uiSettings = uiSettings,
                personalSpace = actualPersonalSpace,
                onLetterButtonClick = typesetterButtons.onLetterButtonClick
            )

            TypesetterButtonsView(
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

}

@Composable
fun TypesetterButtonsView(
    onSortButtonClick: (SortButton) -> Unit,
    onSubmit: () -> Unit,
    fontSize: TextUnit = 30.sp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = CenterHorizontally,
    ) {
        SubmitButton(fontSize, onSubmit, modifier)

        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            SortButton.entries.forEach {
                SortButton(it, onSortButtonClick, modifier, fontSize)
            }
        }
    }
}

@Composable
fun SortButton(
    sortButton: SortButton,
    onSortButtonClick: (SortButton) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 30.sp,
) {
    Button(
        content = { Text(sortButton.label, fontSize = fontSize) },
        onClick = { onSortButtonClick(sortButton) },
        modifier = modifier
    )
}

@Composable
private fun SubmitButton(
    fontSize: TextUnit,
    onSubmit: () -> Unit,
    modifier: Modifier
) {
    Button(
        content = { Text("Submit ⏎", fontSize = fontSize) },
        onClick = onSubmit,
        modifier = modifier
    )
}