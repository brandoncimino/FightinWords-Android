package brava.fightinwords.ui.typesetter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.LetterCase
import brava.fightinwords.ui.MaterialColorPair

object LetterTile {
    val Big = 55.dp
    val Medium = 40.dp
    val Small = 22.dp
}

enum class LetterTileFlavor(
    val materialColorPair: MaterialColorPair
) {
    PollUnslotted(MaterialColorPair.Primary),
    PoolSlotted(MaterialColorPair.SurfaceVariant),
    Galley(MaterialColorPair.Secondary),
    Submission(MaterialColorPair.PrimaryContainer),
    BonusWord(MaterialColorPair.SecondaryContainer)
    ;

    @Composable
    fun buttonColors(): ButtonColors {
        return materialColorPair.buttonColors()
    }
}

@Composable
fun LetterTile(
    letter: String,
    letterCase: LetterCase = LetterCase.Uppercase,
    personalSpace: Dp = LetterTile.Big,
    flavor: LetterTileFlavor,
    onClick: () -> Unit = {},
    buttonEnabled: Boolean = true,
) {
    val tileShape = MaterialTheme.shapes.extraSmall
    val innerSpace = personalSpace * .05f
    val tileSpace = personalSpace - innerSpace
    val downOffset = personalSpace / 70
    val personalFont = with(LocalDensity.current) { (tileSpace).toSp() }

    Box(
        modifier = Modifier.requiredSize(personalSpace)
    ) {
        ElevatedButton(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(innerSpace),
            onClick = onClick,
            contentPadding = PaddingValues(0.dp),
            colors = flavor.buttonColors(),
            shape = tileShape,
            enabled = buttonEnabled
        ) {
            Text(
                text = letterCase.applyTo(letter),
                modifier = Modifier
                    .wrapContentHeight(unbounded = true)
                    .offset(y = downOffset),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                fontSize = personalFont,
            )
        }
    }
}

@Composable
@Preview
fun LetterTilePreview() {
    Column() {
        Row {
            LetterTileFlavor.entries.forEachIndexed { i, flavor ->
                LetterTile(
                    i.toString(),
                    flavor = flavor,
                )

            }
        }
        LetterTile("A", personalSpace = 55.dp, flavor = LetterTileFlavor.PollUnslotted)
        LetterTile("B", personalSpace = 60.dp, flavor = LetterTileFlavor.PoolSlotted)
        LetterTile("I", personalSpace = 35.dp, flavor = LetterTileFlavor.Galley)

    }
}

@Composable
@Preview
fun LetterTileRowPreview() {
    val letters = "yolo".map { it.toString() }

    Row() {
        letters.forEach {
            LetterTile(it, flavor = LetterTileFlavor.PollUnslotted)
        }
    }
}