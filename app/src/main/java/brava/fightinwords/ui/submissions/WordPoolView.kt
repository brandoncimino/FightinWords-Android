package brava.fightinwords.ui.submissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.ui.JetpackBoosters
import brava.fightinwords.ui.PaddingRatio
import brava.fightinwords.ui.obtuseSubmissions
import brava.fightinwords.ui.typesetter.LetterTile
import brava.fightinwords.ui.typesetter.LetterTileFlavor
import kotlin.math.floor

@Composable
fun WordPoolView(
    visibleWords: List<DefinedWordState>,
    modifier: Modifier = Modifier,
    padToLongestWord: Boolean = false,
    wordPadding: PaddingRatio = PaddingRatio(horizontal = .5f, vertical = .2f),
    maxLetterPersonalSpace: Dp = 55.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    onWordClicked: (DefinedWordState) -> Unit = {}
) {
    check(visibleWords.isNotEmpty(), { "Can't render a word pool without any words!" })

    BoxWithConstraints(
        modifier = modifier
    ) {
        val availableSpace = DpSize(this.maxWidth, this.maxHeight)
        val paddedWordLength = when (padToLongestWord) {
            true -> visibleWords.maxOf { it.word.length }
            false -> null
        }

        val letterPersonalSpace = shrinkLettersToFit(
            maxLetterPersonalSpace,
            visibleWords.asSequence().map { paddedWordLength ?: it.word.length },
            PaddingRatio(horizontal = .5f, vertical = .2f),
            availableSpace = availableSpace
        )

        FlowColumn(
            horizontalArrangement = horizontalArrangement,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            for (state in visibleWords) {
                    WordPoolWord(
                        definedWordState = state,
                        letterPersonalSpace = letterPersonalSpace,
                        wordPadding = wordPadding,
                        onClick = onWordClicked
                    )
            }
        }
    }
}

@Composable
fun WordPoolWord(
    definedWordState: DefinedWordState,
    letterPersonalSpace: Dp,
    wordPadding: PaddingRatio,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onClick: (DefinedWordState) -> Unit = {}
) {
    val word = definedWordState.word

    Row(
        modifier = modifier
            .padding(wordPadding * letterPersonalSpace)
            .clickable {
                onClick(definedWordState)
            },
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


@Preview(showBackground = true)
@Preview(showBackground = true, heightDp = 300)
@Preview(showBackground = true, heightDp = 100)
@Preview(showBackground = true, heightDp = 200)
@Composable
fun ScoreboardViewPreview() {
    WordPoolView(
        obtuseSubmissions(),
        horizontalArrangement = Arrangement.Center
    )
}

fun canWordsFit(
    letterPersonalSpace: Dp,
    wordLengths: Sequence<Int>,
    wordPadding: PaddingRatio,
    availableSpace: DpSize
): Boolean {
    val wordHeight = letterPersonalSpace * (1 + wordPadding.vertical)
    val maxWordsPerColumn = floor((availableSpace.height / wordHeight)).toInt()

    if (maxWordsPerColumn <= 0) {
        return false
    }

    var widthLeft = availableSpace.width
    for (column in wordLengths.chunked(maxWordsPerColumn)) {
        val columnWidth = letterPersonalSpace * (column.max() + wordPadding.horizontal)
        widthLeft -= columnWidth
        if (widthLeft < 0.dp) {
            return false
        }
    }

    return true
}

fun shrinkLettersToFit(
    maxLetterPersonalSpace: Dp,
    wordLengths: Sequence<Int>,
    wordPadding: PaddingRatio,
    availableSpace: DpSize,
    decrement: Dp = 1.dp
): Dp {
    assert(decrement > 0.dp)

    return JetpackBoosters.shrinkToFit(
        maxValue = maxLetterPersonalSpace,
        checkFit = {
            canWordsFit(
                it,
                wordLengths,
                wordPadding,
                availableSpace
            )
        },
        bigShrinker = { it * .9f },
        littleGrower = { it + 1.dp }
    )
}