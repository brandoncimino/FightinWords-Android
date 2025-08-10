package brava.fightinwords.ui.submissions


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.isBonusWord
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.PreviewHelpers
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.PreviewHelpers.nuts
import brava.fightinwords.ui.theme.GameIcons
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinitionBox(
    definedWord: DefinedWordState?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        when (definedWord) {
            null -> Text(" ")
            else -> {
                DefinitionView(
                    definedWord,
                    modifier = Modifier.padding(contentPadding)
                )
            }
        }
    }
}

@Composable
fun DefinitionView(
    definedWord: DefinedWordState,
    modifier: Modifier = Modifier,
    wordStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyLarge
        .copy(fontStyle = FontStyle.Italic),
    definitionStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefinitionHeadline(definedWord, wordStyle, subtitleStyle)

            if (definedWord.isBonusWord) {
                BonusWordIcon(Modifier.align(Alignment.TopEnd))
            }
        }

        Text(
            text = definedWord.wordDefinition.definition,
            style = definitionStyle,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun DefinitionHeadline(
    definedWord: DefinedWordState,
    wordStyle: TextStyle,
    subtitleStyle: TextStyle,
) {
    Text(
        text = buildAnnotatedString {
            this.append(definedWord.word.toString() + " ")

            val parts = sequence {
                val partOfSpeech = definedWord.wordDefinition.partOfSpeech?.lowercase()
                if (partOfSpeech != null) {
                    yield(partOfSpeech)
                }

                if (definedWord is Accepted) {
                    yield("${definedWord.points} points")
                }
            }.toList().toList()

            if (parts.isNotEmpty()) {
                withStyle(
                    subtitleStyle.toSpanStyle()
                ) {
                    this.append(
                        parts.fastJoinToString(
                            separator = ", ",
                            prefix = "(",
                            postfix = ")"
                        )
                    )
                }
            }
        },
        style = wordStyle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonusWordIcon(modifier: Modifier = Modifier, painter: Painter = painterResource(GameIcons.BonusIcon)) {
    Box(modifier) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text("Bonus Word")
                }
            },
            state = rememberTooltipState(),
            modifier = modifier
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

class WordDefinitionPreviews : PreviewParameterProvider<DefinedWordState> {
    override val values: Sequence<DefinedWordState>
        get() = sequenceOf(
            Accepted(deez, 99),
            Unplayed(nuts),
            Unplayed(PreviewHelpers.longDefinition.copy(isNaspaWord = true)),
            Unplayed(
                Json.decodeFromString<WordDefinition>(Json.encodeToString(deez))
            ),
            Accepted(PreviewHelpers.redactedDefinition, 21)
        )
}

@Preview(showBackground = true)
@Composable
fun DefinitionViewPreview(
    @PreviewParameter(WordDefinitionPreviews::class) definedWordState: DefinedWordState,
) {
    DefinitionBox(
        definedWordState
    )
}