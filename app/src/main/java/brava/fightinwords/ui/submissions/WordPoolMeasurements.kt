package brava.fightinwords.ui.submissions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.times
import brava.fightinwords.ui.JetpackBoosters
import brava.fightinwords.ui.PaddingRatio
import brava.fightinwords.ui.calculateHorizontalPadding
import brava.fightinwords.ui.calculateVerticalPadding
import kotlin.math.floor

data class WordPoolMeasurements(
    val availableSpace: DpSize,
    val letterPersonalSpace: Dp,
    val wordPadding: PaddingRatio,
    val wordLengths: List<Int>,
) {

    val maxWordsPerColumn: Int get() = floor((availableSpace.height / wordHeight)).toInt()
    val requiredColumns get() = wordLengths.asSequence().chunked(maxWordsPerColumn).count()
    val exactWordPadding = wordPadding * letterPersonalSpace
    val wordHeight get() = letterPersonalSpace + exactWordPadding.calculateVerticalPadding()
    val columnWidthsInLetters get() = wordLengths.asSequence().chunked(maxWordsPerColumn, { lengths -> lengths.max() })
    val widthFromLetters get() = columnWidthsInLetters.sum() * letterPersonalSpace
    val widthFromPadding get() = exactWordPadding.calculateHorizontalPadding() * requiredColumns
    val requiredWidth get() = widthFromLetters + widthFromPadding

    val fits get() = requiredWidth < availableSpace.width

    fun resize(transformation: (Dp) -> Dp) = copy(
        letterPersonalSpace = transformation(letterPersonalSpace)
    )

    companion object {
        fun shrinkLettersToFit(
            maxLetterPersonalSpace: Dp,
            minLetterPersonalSpace: Dp,
            wordPadding: PaddingRatio,
            wordLengths: List<Int>,
            availableSpace: DpSize,
            bigShrinker: (Dp) -> Dp,
            littleGrower: (Dp) -> Dp,
        ): WordPoolMeasurements {
            val max = WordPoolMeasurements(
                availableSpace,
                maxLetterPersonalSpace,
                wordPadding,
                wordLengths
            )

            return JetpackBoosters.shrinkToFit(
                maxValue = max,
                minValue = max.copy(letterPersonalSpace = minLetterPersonalSpace),
                checkFit = { it.fits },
                bigShrinker = { it.resize(bigShrinker) },
                littleGrower = { it.resize(littleGrower) }
            )
        }
    }
}