package brava.fightinwords.ui.submissions


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.data.Word
import brava.fightinwords.gameplay.scoring.ScoreboardWord
import brava.fightinwords.ui.PaddingRatio
import brava.fightinwords.ui.PreviewHelpers
import brava.fightinwords.ui.theme.Pink80
import brava.fightinwords.ui.typesetter.LetterTile
import com.google.common.collect.Lists

@Composable
fun ScrollableWordPool(
    visibleWords: List<ScoreboardWord>,
    modifier: Modifier = Modifier,
    maxLetterPersonalSpace: Dp = LetterTile.Big,
    minLetterPersonalSpace: Dp = LetterTile.Small,
    wordPadding: PaddingRatio = defaultWordPadding,
    onWordClick: (Word) -> Unit = {},
) {
    BoxWithConstraints(modifier) {
        val availableSpace = DpSize(this.maxWidth, this.maxHeight)
        val letterSize = WordPoolMeasurements.shrinkLettersToFit(
            maxLetterPersonalSpace,
            minLetterPersonalSpace,
            wordPadding,
            Lists.transform(visibleWords) { it.word.length },
            availableSpace,
            bigShrinker = { it - 10.dp },
            littleGrower = { it + 5.dp }
        )

        val rowHeight = letterSize.wordHeight

        LazyHorizontalGrid(
            rows = GridCells.FixedSize(rowHeight)
        ) {
            items(visibleWords) {
                WordPoolWord(
                    it,
                    letterSize.letterPersonalSpace,
                    onClick = onWordClick,
                    modifier = Modifier.padding(
                        letterSize.exactWordPadding
                    )
                )
            }
        }
    }
}


@Preview(showBackground = true, heightDp = 300, showSystemUi = true)
@Composable
fun LazyWordPoolPreview() {
    Box(
        modifier = Modifier
            .background(Pink80),
    ) {

        ScrollableWordPool(
            PreviewHelpers.ledgermanUiState().visibleWords,
        )
    }

}