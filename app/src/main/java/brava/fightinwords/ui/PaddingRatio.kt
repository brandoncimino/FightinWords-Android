package brava.fightinwords.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.times
import brava.fightinwords.botlin.constrain


data class DpRatio(
    val ratio: Float,
    val min: Dp? = null,
    val max: Dp? = null,
) {
    operator fun times(relativeTo: Dp): Dp {
        return (ratio * relativeTo).constrain(min, max)
    }

    companion object {
        fun Float.dpRatio(min: Dp? = null, max: Dp? = null) = DpRatio(this, min, max)
    }
}

/**
 * A "unitless" equivalent to [PaddingValues] _(which has the unit [Dp])_
 */
data class PaddingRatio(
    val start: DpRatio,
    val top: DpRatio,
    val end: DpRatio,
    val bottom: DpRatio,
) {
    @Suppress("unused")
    constructor(
        all: DpRatio,
    ) : this(all, all, all, all)

    constructor(
        horizontal: DpRatio,
        vertical: DpRatio,
    ) : this(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

    operator fun times(relativeTo: Dp): PaddingValues {
        return PaddingValues(
            start = start * relativeTo,
            top = top * relativeTo,
            end = end * relativeTo,
            bottom = bottom * relativeTo
        )
    }
}

/**
 * [PaddingValues.calculateTopPadding] + [PaddingValues.calculateBottomPadding]
 */
fun PaddingValues.calculateVerticalPadding() = calculateTopPadding() + calculateBottomPadding()

/**
 * [calculateStartPadding] + [calculateEndPadding]
 */
fun PaddingValues.calculateHorizontalPadding() = calculateStartPadding(Ltr) + calculateEndPadding(Ltr)

