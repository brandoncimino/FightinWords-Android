package brava.fightinwords.ui.submissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.WordCategory
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.data.Word.Companion.indices
import brava.fightinwords.gameplay.scoring.ScoreboardWord
import brava.fightinwords.gameplay.scoring.ScoreboardWordVisibility
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.typesetter.LetterTile
import brava.fightinwords.ui.typesetter.LetterTileFlavor

@Composable
fun WordPoolWord(
    wordState: ScoreboardWord,
    letterPersonalSpace: Dp,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onClick: (Word) -> Unit = {},
) {
    val word = wordState.word

    Row(
        modifier = modifier
            .clickable(wordState.visibility == ScoreboardWordVisibility.Full) {
                onClick(word)
            }
            .requiredHeight(letterPersonalSpace),
        horizontalArrangement = horizontalArrangement
    ) {

        val flavor = when (wordState.category) {
            WordCategory.Bonus -> LetterTileFlavor.BonusWord
            WordCategory.Core  -> LetterTileFlavor.Submission
        }

        for (letter in word.indices) {
            LetterTile(
                letter = when (wordState.visibility) {
                    ScoreboardWordVisibility.Full   -> word[letter].toString()
                    ScoreboardWordVisibility.Masked -> " "
                },
                flavor = flavor,
                personalSpace = letterPersonalSpace,
                onClick = { onClick(word) },
                buttonEnabled = wordState.visibility != ScoreboardWordVisibility.Masked
            )
        }
    }
}

@Preview
@Composable
fun WordPoolWordPreview() {
    WordPoolWord(
        ScoreboardWord(deez.word, ScoreboardWordVisibility.Full, WordCategory.Bonus),
        letterPersonalSpace = 10.dp
    )
}