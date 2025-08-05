package brava.fightinwords.ui.submissions

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import brava.fightinwords.gameplay.scoring.FilterState
import brava.fightinwords.gameplay.scoring.WordFilter

@Composable
fun WordFilterButton(
    onClick: (WordFilter.State) -> Unit,
    wordFilter: WordFilter.State,
    miseEnScene: MiseEnScene,
    modifier: Modifier = Modifier,
) {
    val stagePresence = when (wordFilter.filterState) {
        FilterState.ActiveExplicitly -> miseEnScene.spotlight
        FilterState.ActiveImplicitly -> miseEnScene.ensemble
        FilterState.Inactive         -> miseEnScene.backup
    }
    ElevatedButton(
        onClick = { onClick(wordFilter) },
        modifier = modifier,
        colors = ButtonColors(
            containerColor = stagePresence.container,
            contentColor = stagePresence.content,
            disabledContainerColor = stagePresence.container,
            disabledContentColor = stagePresence.content,
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = stagePresence.elevation
        )
    ) {
        Text(
            wordFilter.wordFilter.label
        )
    }
}