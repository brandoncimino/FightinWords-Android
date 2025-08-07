package brava.fightinwords.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import brava.fightinwords.gameplay.LetterCase

data class UiSettings(
    val maxButtonsPerRow: Int? = null,
    val comfortableButtonsPerRow: Int = 6,
    val letterButtonCase: LetterCase = LetterCase.Uppercase,
    val rejectionDisplay: RejectionDisplay = RejectionDisplay.Exclude,
    val sectionOrder: List<UiSection> = UiSection.entries,
) {

    enum class RejectionDisplay {
        Exclude,
        Include,
    }

    enum class UiSection {
        FilterButtons,
        Scoreboard,
        Typesetter,
        Definition,
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