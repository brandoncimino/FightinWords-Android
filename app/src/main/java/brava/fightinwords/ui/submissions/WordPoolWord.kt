package brava.fightinwords.ui.submissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.WordState
import brava.fightinwords.ui.typesetter.LetterTile
import brava.fightinwords.ui.typesetter.LetterTileFlavor

@Composable
fun WordPoolWord(
    wordState: WordState,
    letterPersonalSpace: Dp,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onClick: (WordState) -> Unit = {},
) {
    val word = wordState.word

    Row(
        modifier = modifier
            .clickable {
                onClick(wordState)
            }
            .requiredHeight(letterPersonalSpace),
        horizontalArrangement = horizontalArrangement
    ) {
        val flavor = when (wordState is DefinedWordState && wordState.wordDefinition.isNaspaWord) {
            true -> LetterTileFlavor.Submission
            false -> LetterTileFlavor.BonusWord
        }
        for (letter in word) {
            LetterTile(
                letter = when (wordState) {
                    is Accepted -> letter.toString()
                    else        -> " "
                },
                flavor = flavor,
                personalSpace = letterPersonalSpace,
                onClick = { onClick(wordState) },
                buttonEnabled = wordState is Accepted
            )
        }
    }
}