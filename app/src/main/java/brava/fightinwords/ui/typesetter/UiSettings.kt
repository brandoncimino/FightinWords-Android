package brava.fightinwords.ui.typesetter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import brava.fightinwords.gameplay.LetterCase

data class UiSettings(
    val maxButtonsPerRow: Int? = null,
    val comfortableButtonsPerRow: Int = 6,
    val letterButtonCase: LetterCase = LetterCase.Uppercase,
    val rejectionDisplay: RejectionDisplay = RejectionDisplay.Include,
    val sectionOrder: Set<UiSection> = UiSection.entries.toSet()
) {

    enum class RejectionDisplay {
        Include,
        Exclude
    }

    enum class UiSection {
        Scoreboard,
        Typesetter,
        Definition
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