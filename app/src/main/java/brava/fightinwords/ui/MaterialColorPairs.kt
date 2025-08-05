package brava.fightinwords.ui

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import brava.fightinwords.ui.MaterialColor.*

enum class MaterialColor {
    Primary,
    OnPrimary,
    PrimaryContainer,
    OnPrimaryContainer,
    InversePrimary,
    Secondary,
    OnSecondary,
    SecondaryContainer,
    OnSecondaryContainer,
    Tertiary,
    OnTertiary,
    TertiaryContainer,
    OnTertiaryContainer,
    Background,
    OnBackground,
    Surface,
    OnSurface,
    SurfaceVariant,
    OnSurfaceVariant,
    SurfaceTint,
    InverseSurface,
    InverseOnSurface,
    Error,
    OnError,
    ErrorContainer,
    OnErrorContainer,
    Outline,
    OutlineVariant,
    Scrim,
    SurfaceBright,
    SurfaceDim,
    SurfaceContainer,
    SurfaceContainerHigh,
    SurfaceContainerHighest,
    SurfaceContainerLow,
    SurfaceContainerLowest,
    ;

    fun from(colorScheme: ColorScheme): Color {
        return when (this) {
            Primary -> colorScheme.primary
            OnPrimary -> colorScheme.onPrimary
            PrimaryContainer -> colorScheme.primaryContainer
            OnPrimaryContainer -> colorScheme.onPrimaryContainer
            InversePrimary -> colorScheme.inversePrimary
            Secondary -> colorScheme.secondary
            OnSecondary -> colorScheme.onSecondary
            SecondaryContainer -> colorScheme.secondaryContainer
            OnSecondaryContainer -> colorScheme.onSecondaryContainer
            Tertiary -> colorScheme.tertiary
            OnTertiary -> colorScheme.onTertiary
            TertiaryContainer -> colorScheme.tertiaryContainer
            OnTertiaryContainer -> colorScheme.onTertiaryContainer
            Background -> colorScheme.background
            OnBackground -> colorScheme.onBackground
            Surface -> colorScheme.surface
            OnSurface -> colorScheme.onSurface
            SurfaceVariant -> colorScheme.surfaceVariant
            OnSurfaceVariant -> colorScheme.onSurfaceVariant
            SurfaceTint -> colorScheme.surfaceTint
            InverseSurface -> colorScheme.inverseSurface
            InverseOnSurface -> colorScheme.inverseOnSurface
            Error -> colorScheme.error
            OnError -> colorScheme.onError
            ErrorContainer -> colorScheme.errorContainer
            OnErrorContainer -> colorScheme.onErrorContainer
            Outline -> colorScheme.outline
            OutlineVariant -> colorScheme.outlineVariant
            Scrim -> colorScheme.scrim
            SurfaceBright -> colorScheme.surfaceBright
            SurfaceDim -> colorScheme.surfaceDim
            SurfaceContainer -> colorScheme.surfaceContainer
            SurfaceContainerHigh -> colorScheme.surfaceContainerHigh
            SurfaceContainerHighest -> colorScheme.surfaceContainerHighest
            SurfaceContainerLow -> colorScheme.surfaceContainerLow
            SurfaceContainerLowest -> colorScheme.surfaceContainerLowest
        }
    }

    companion object {
        operator fun ColorScheme.get(materialColor: MaterialColor): Color = materialColor.from(this)
    }
}

enum class MaterialColorPair(
    private val background: MaterialColor,
    private val content: MaterialColor
) {
    Primary(MaterialColor.Primary, OnPrimary),
    PrimaryContainer(MaterialColor.PrimaryContainer, OnPrimaryContainer),
    Secondary(MaterialColor.Secondary, OnSecondary),
    SecondaryContainer(MaterialColor.SecondaryContainer, OnSecondaryContainer),
    Tertiary(MaterialColor.Tertiary, OnTertiary),
    TertiaryContainer(MaterialColor.TertiaryContainer, OnTertiaryContainer),
    Surface(MaterialColor.Surface, OnSurface),
    SurfaceVariant(MaterialColor.SurfaceVariant, OnSurfaceVariant),
    Error(MaterialColor.Error, OnError),
    ErrorContainer(MaterialColor.ErrorContainer, OnErrorContainer),
    ;

    @Composable
    fun background(): Color = background.from(MaterialTheme.colorScheme)

    @Composable
    fun content(): Color = content.from(MaterialTheme.colorScheme)

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = background(),
        contentColor = content()
    )
}