package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.WordState
import brava.fightinwords.ui.DpRatio.Companion.dpRatio
import brava.fightinwords.ui.PaddingRatio
import brava.fightinwords.ui.PreviewHelpers

@Composable
fun LedgermanWordPool(
    visibleWords: List<WordState>,
    modifier: Modifier = Modifier,
    onFocusWord: (WordState) -> Unit = {},
    wordPadding: PaddingRatio = defaultWordPadding,
) {
    if (visibleWords.isEmpty()) {
        Text(
            text = "All words have been filtered out.",
            fontStyle = FontStyle.Italic,
            modifier = modifier
        )
    } else {
        ScrollableWordPool(
            visibleWords = visibleWords,
            modifier = modifier,
            onWordClick = {
                if (it is DefinedWordState) {
                    onFocusWord(it)
                }
            },
            wordPadding = wordPadding
        )
    }
}

val defaultWordPadding = PaddingRatio(
    horizontal = .5f.dpRatio(),
    vertical = .1f.dpRatio()
)

@Preview(showBackground = true, heightDp = 600)
@Composable
fun LedgermanViewPreview() {
    val ledgermanUiState = PreviewHelpers.ledgermanUiState()
    Column {
        ScoreboardWordFilters(
            wordFilterStates = ledgermanUiState.wordFilterStates,
            onWordFilterClicked = {}
        )

        LedgermanWordPool(ledgermanUiState.visibleWords)
    }
}