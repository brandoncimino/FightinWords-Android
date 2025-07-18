package brava.fightinwords.ui.typesetter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.data.Letter
import brava.fightinwords.ui.swaggins

data class LetterButtonState(
    val letter: Letter,
    val isSlotted: Boolean
) {
    constructor(pair: Pair<Letter, Boolean>) : this(pair.first, pair.second)
}

@Composable
fun LetterPoolView(
    letterButtonStates: List<LetterButtonState>,
    uiSettings: UiSettings = UiSettings(),
    personalSpace: Dp,
    onLetterButtonClick: (index: Int) -> Unit
) {
    Column(
        horizontalAlignment = CenterHorizontally,
    ) {

        FlowRow(
            maxItemsInEachRow = uiSettings.maxButtonsPerRow ?: Int.MAX_VALUE
        ) {
            letterButtonStates.forEachIndexed { index, letterButtonState ->
                LetterTile(
                    letter = letterButtonState.letter.toString(),
                    letterCase = uiSettings.letterButtonCase,
                    personalSpace = personalSpace,
                    flavor = when (letterButtonState.isSlotted) {
                        true -> LetterTileFlavor.PoolSlotted
                        false -> LetterTileFlavor.PollUnslotted
                    },
                    onClick = { onLetterButtonClick(index) }
                )
            }
        }
    }

}

@Composable
@Preview(device = Devices.PIXEL, showSystemUi = true)
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
fun LetterPoolViewPreview(
    @PreviewParameter(UiSettingsPreviewProvider::class) uiSettings: UiSettings
) {
    LetterPoolView(
        swaggins().letterButtonStates,
        uiSettings,
        55.dp,
        {}
    )
}