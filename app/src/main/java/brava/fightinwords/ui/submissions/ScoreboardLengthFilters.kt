package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import brava.fightinwords.gameplay.scoring.Scoreboard
import brava.fightinwords.ui.MaterialColorPair

@Composable
fun ScoreboardLengthFilterButton(
    wordLength: Int,
    filterState: Scoreboard.FilterState,
    onClick: (wordLength: Int) -> Unit
) {
    val colors: MaterialColorPair = when (filterState) {
        Scoreboard.FilterState.Disabled -> MaterialColorPair.SurfaceVariant
        Scoreboard.FilterState.EnabledImplicitly -> MaterialColorPair.Primary
        Scoreboard.FilterState.EnabledExplicitly -> MaterialColorPair.ErrorContainer
    }

    val isLengthVisible = filterState.isEnabled
    Button(
        onClick = { onClick(wordLength) },
        colors = colors.buttonColors()
    ) {
        Text(wordLength.toString())
    }
}

@Composable
fun ScoreboardLengthFilters(
    lengthFilterStates: List<Pair<Int, Scoreboard.FilterState>>,
    onLengthFilterButtonClick: (wordLength: Int) -> Unit
) {
    Row {
        lengthFilterStates.forEach { (wordLength, filterState) ->
            ScoreboardLengthFilterButton(
                wordLength,
                filterState,
                onLengthFilterButtonClick
            )
        }
    }
}

@Preview
@Composable
fun ScoreboardLengthFiltersPreview() {

}