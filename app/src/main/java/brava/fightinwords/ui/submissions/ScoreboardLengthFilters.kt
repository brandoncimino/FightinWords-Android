package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.ui.PreviewHelpers

@Composable
fun ScoreboardWordFilters(
    wordFilterStates: List<WordFilter.State>,
    onWordFilterClicked: (WordFilter.State) -> Unit,
    modifier: Modifier = Modifier,
    miseEnScene: MiseEnScene = miseEnScene(),
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        wordFilterStates.forEach {
            WordFilterButton(
                onWordFilterClicked,
                it,
                miseEnScene = miseEnScene,
                modifier = Modifier.padding(3.dp)
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