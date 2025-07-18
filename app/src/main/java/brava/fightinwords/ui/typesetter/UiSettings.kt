package brava.fightinwords.ui.typesetter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import brava.fightinwords.gameplay.LetterCase

data class UiSettings(
    val maxButtonsPerRow: Int? = null,
    val letterButtonCase: LetterCase = LetterCase.Uppercase,
    val rejectionDisplay: RejectionDisplay = RejectionDisplay.Include
) {
    companion object {
        const val ComfortableButtonsPerRow: Int = 6
    }

    enum class RejectionDisplay {
        Include,
        Exclude
    }
}

class UiSettingsPreviewProvider : PreviewParameterProvider<UiSettings> {
    override val values: Sequence<UiSettings>
        get() = sequenceOf(
            UiSettings(
                maxButtonsPerRow = 4,
            ),
            UiSettings(
                letterButtonCase = LetterCase.Uppercase
            ),
            UiSettings(
                maxButtonsPerRow = 10
            )
        )
}