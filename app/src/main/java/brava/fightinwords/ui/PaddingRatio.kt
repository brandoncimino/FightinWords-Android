package brava.fightinwords.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times

/**
 * A "unitless" equivalent to [PaddingValues] _(which has the unit [Dp])_
 */
data class PaddingRatio(
    val start: Float,
    val top: Float,
    val end: Float,
    val bottom: Float
) {
    constructor(
        all: Float
    ) : this(all, all, all, all)

    constructor(
        horizontal: Float,
        vertical: Float
    ) : this(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

    val horizontal: Float
        get() = start + end
    val vertical: Float
        get() = bottom + top

    operator fun times(dp: Dp): PaddingValues {
        return PaddingValues(
            start = start * dp,
            top = top * dp,
            end = end * dp,
            bottom = bottom * dp
        )
    }
}