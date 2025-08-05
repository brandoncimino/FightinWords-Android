package brava.fightinwords.ui.submissions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import brava.fightinwords.gameplay.scoring.FilterState
import brava.fightinwords.gameplay.scoring.WordFilter

@Composable
fun OneChoiceWordFilters(
    filterStates: List<WordFilter.State>,
    onClick: (WordFilter.State) -> Unit,
    miseEnScene: MiseEnScene = miseEnScene(),
) {
    Row {
        for (filter in filterStates) {
            WordFilterButton(
                onClick,
                filter,
                miseEnScene
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun OneChoiceWordFiltersPreview() {

    OneChoiceWordFilters(
        onClick = {},
        filterStates = FilterState.entries.map {
            WordFilter.State(
                WordFilter.LengthFilter(3 + it.ordinal),
                it
            )
        },

        )
}

enum class StarPower {
    /**
     * One of many, where nobody stands out in particular.
     */
    Ensemble,

    /**
     * Currently the star of the show, with everyone else as [Backup].
     */
    Spotlight,

    /**
     * There to support the [Spotlight], while fading into the background.
     */
    Backup,
}

data class StagePresence(
    val container: Color = Color.Unspecified,
    val content: Color = Color.Unspecified,
    val elevation: Dp = Dp.Unspecified,
    val border: BorderStroke? = null,
) {
    companion object {
        val Unspecified = StagePresence()

        private const val low = 1f
        private const val mid = 4f
        private const val high = 10f

        private var cachedEnsembleDefaults: StagePresence? = null
        private var cachedBackupDefaults: StagePresence? = null
        private var cachedSpotlightDefaults: StagePresence? = null

        @Composable
        private fun MaterialTheme.computeEnsembleDefaults(): StagePresence {
            return StagePresence(
                colorScheme.primary,
                colorScheme.onPrimary,
                mid.dp
            )
        }

        @Composable
        private fun MaterialTheme.computeBackupDefaults(): StagePresence {
            return StagePresence(
                Color.Transparent,
                colorScheme.primary,
                low.dp,
                ButtonDefaults.outlinedButtonBorder()
            )
        }

        @Composable
        private fun MaterialTheme.computeSpotlightDefaults(): StagePresence {
            return StagePresence(
                colorScheme.primary,
                colorScheme.onPrimary,
                high.dp
            )
        }

        @Composable
        fun ensemble(): StagePresence {
            return cachedEnsembleDefaults
                   ?: MaterialTheme.computeEnsembleDefaults()
                       .also { cachedEnsembleDefaults = it }
        }

        @Composable
        fun ensemble(
            container: Color = Color.Unspecified,
            content: Color = Color.Unspecified,
            elevation: Dp = Dp.Unspecified,
            border: BorderStroke? = null,
        ): StagePresence {
            return withFallback(
                { ensemble() },
                container,
                content,
                elevation,
                border
            )
        }

        @Composable
        fun backup(): StagePresence {
            return cachedBackupDefaults
                   ?: MaterialTheme.computeBackupDefaults()
                       .also { cachedBackupDefaults = it }
        }

        @Composable
        fun backup(
            container: Color = Color.Unspecified,
            content: Color = Color.Unspecified,
            elevation: Dp = Dp.Unspecified,
            border: BorderStroke? = null,
        ): StagePresence {
            return withFallback({ backup() }, container, content, elevation, border)
        }

        @Composable
        private inline fun withFallback(
            fallback: @Composable () -> StagePresence,
            container: Color,
            content: Color,
            elevation: Dp,
            border: BorderStroke?,
        ): StagePresence {
            return StagePresence(
                container.takeOrElse { fallback().container },
                content.takeOrElse { fallback().content },
                elevation.takeOrElse { fallback().elevation },
                border ?: fallback().border
            )
        }


        @Composable
        fun spotlight(): StagePresence {
            return cachedSpotlightDefaults
                   ?: MaterialTheme.computeSpotlightDefaults()
                       .also { cachedSpotlightDefaults = it }
        }

        @Composable
        fun spotlight(
            container: Color = Color.Unspecified,
            content: Color = Color.Unspecified,
            elevation: Dp = Dp.Unspecified,
            border: BorderStroke? = null,
        ): StagePresence {
            return withFallback(
                { spotlight() },
                container,
                content,
                elevation, border
            )
        }
    }
}

data class MiseEnScene(
    val ensemble: StagePresence,
    val spotlight: StagePresence,
    val backup: StagePresence,
)

@Composable
fun miseEnScene(
    ensemble: StagePresence = StagePresence.ensemble(),
    spotlight: StagePresence = StagePresence.spotlight(),
    backup: StagePresence = StagePresence.backup(),
): MiseEnScene {
    return MiseEnScene(
        ensemble,
        spotlight,
        backup
    );
}