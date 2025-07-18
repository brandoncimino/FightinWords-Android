package brava.fightinwords.ui.typesetter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.ui.swaggins

@Composable
fun GalleyView(
    composingStick: Word,
    uiSettings: UiSettings,
    letterTilePersonalSpace: Dp,
    onClick: (index: Int) -> Unit
) {
    Row(
        modifier = Modifier.border(
            1.dp, MaterialTheme.colorScheme.surface
        )
            .height(letterTilePersonalSpace)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        composingStick.forEachIndexed { i, it ->
            LetterTile(
                letter = it.toString(),
                letterCase = uiSettings.letterButtonCase,
                personalSpace = letterTilePersonalSpace,
                flavor = LetterTileFlavor.Galley,
                onClick = { onClick(i) }
            )
        }
    }
}

class GalleyPreviews : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf(
            "yolo",
            "swaggins",
            "ab",
            ""
        )

}

@Composable
@Preview
fun GalleyViewPreview2() {
    GalleyView(
        swaggins().galley,
        uiSettings = UiSettings(),
        letterTilePersonalSpace = 55.dp,
        {}
    )
}

@Composable
@Preview(
    device = Devices.PIXEL_FOLD
)
fun GalleyViewPreview(
    @PreviewParameter(GalleyPreviews::class)
    letters: String
) {
    GalleyView(
        letters.map { it.toLetter() }.toWord(),
        UiSettings(),
        55.dp,
        {}
    )
}
