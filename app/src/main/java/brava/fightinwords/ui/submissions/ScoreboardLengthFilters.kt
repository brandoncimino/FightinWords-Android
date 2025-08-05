package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.ui.PreviewHelpers

@Composable
fun ScoreboardWordFilters(
    wordFilterStates: List<WordFilter.State>,
    onWordFilterClicked: (WordFilter.State) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        wordFilterStates.forEach {
            WordFilterButton(
                onWordFilterClicked,
                it,
                miseEnScene()
            )
        }
    }
}

@Preview
@Composable
fun ScoreboardLengthFiltersPreview() {
    ScoreboardWordFilters(
        PreviewHelpers.wordFilterStates(),
        {}
    )
}