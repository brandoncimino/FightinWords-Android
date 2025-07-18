package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.ui.obtuseSubmissions
import brava.fightinwords.ui.typesetter.LetterTile
import brava.fightinwords.ui.typesetter.LetterTileFlavor
import kotlin.math.floor
import kotlin.math.max

@Composable
fun WordPoolView(
    playableWords: List<DefinedWordState>,
    modifier: Modifier = Modifier,
    includeUnsubmitted: Boolean = true,
    paddedWordLength: Int? = null,
    minimumIntraColumnPadding: Float = 1f,
    preferredColumns: Int = 3,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {

    BoxWithConstraints(
        modifier = modifier
    ) {
        val maxHeight = this.maxHeight
        val longestWord = max(paddedWordLength ?: Int.MIN_VALUE, playableWords.maxOf { it.word.size })

        val wordPoolSizes = calculateWordPoolSizes(
            maxWidth,
            maxHeight,
            longestWord,
            preferredColumnCount = preferredColumns,
            minimumIntraColumnPadding = minimumIntraColumnPadding,
            wordCount = playableWords.size,
        )

        FlowColumn(
            horizontalArrangement = horizontalArrangement,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            for (state in playableWords) {
                if (state is Accepted || includeUnsubmitted) {
                    WordPoolWord(
                        definedWordState = state,
                        letterPersonalSpace = wordPoolSizes.letterPersonalSpace,
                        paddedWordLength = longestWord,
                    )
                }
            }
        }
    }
}

fun calculateWordPoolSizes(
    maxWidth: Dp,
    maxHeight: Dp,
    longestWordLength: Int,
    minimumIntraColumnPadding: Float,
    wordCount: Int,
    preferredColumnCount: Int = 3,
    maxColumnCount: Int = Int.MAX_VALUE,
): WordPoolSizes {
    for (colCount in preferredColumnCount..maxColumnCount) {
        val wordPoolSizes = calculateWordSlots(
            maxWidth,
            maxHeight,
            longestWordLength,
            minimumIntraColumnPadding,
            colCount
        )

        if (wordPoolSizes.maxWords >= wordCount) {
            return wordPoolSizes
        }
    }

    throw IllegalStateException("How was it not possible to fit $wordCount words into $maxColumnCount columns?!")
}

data class WordPoolSizes(
    val letterPersonalSpace: Dp,
    val columnCount: Int,
    val maxWords: Int
)

fun calculateWordSlots(
    maxWidth: Dp,
    maxHeight: Dp,
    longestWordLength: Int,
    minimumIntraColumnPadding: Float,
    columnCount: Int
): WordPoolSizes {
    val lettersPerRow = (columnCount * longestWordLength) + ((columnCount - 1) * minimumIntraColumnPadding)
    val letterSize = maxWidth / lettersPerRow

    val maxRows = floor(maxHeight / letterSize).toInt()
    val maxWords = columnCount * maxRows
    return WordPoolSizes(
        letterSize,
        columnCount,
        maxWords
    )
}

@Composable
fun WordPoolWord(
    definedWordState: DefinedWordState,
    letterPersonalSpace: Dp,
    modifier: Modifier = Modifier,
    paddedWordLength: Int? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {

    val word = definedWordState.word

    Row(
        modifier = modifier
            .size(
                height = letterPersonalSpace,
                width = letterPersonalSpace * (paddedWordLength ?: word.size)
            ),
        horizontalArrangement = horizontalArrangement
    ) {
        for (letter in word) {
            LetterTile(
                letter = when (definedWordState) {
                    is Accepted -> letter.toString()
                    else -> " "
                },
                flavor = when (definedWordState.wordDefinition.isNaspaWord) {
                    true -> LetterTileFlavor.Submission
                    false -> LetterTileFlavor.BonusWord
                },
                personalSpace = letterPersonalSpace,
            )
        }
    }
}


@Preview(showBackground = true /*showSystemUi = true*/)
@Preview(showBackground = true, /*showSystemUi = true,*/ /*device = PIXEL_FOLD,*/ heightDp = 300)
@Preview(showBackground = true, /*showSystemUi = true,*/ /*device = PIXEL_FOLD,*/ heightDp = 100)
@Preview(showBackground = true, /*showSystemUi = true,*/ /*device = PIXEL_FOLD,*/ heightDp = 200)
@Composable
fun ScoreboardViewPreview() {
//    Box(Modifier.height(350.dp).fillMaxWidth()) {
    WordPoolView(
        obtuseSubmissions()
    )
//    }
}