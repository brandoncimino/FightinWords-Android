package brava.fightinwords.ui.submissions


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLinkStyles
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
import brava.fightinwords.gameplay.WordCategory
import brava.fightinwords.gameplay.data.appendWord
import brava.fightinwords.gameplay.scoring.FocusedWord
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup.Companion.requireDefinition
import brava.fightinwords.gameplay.wordlookup.WordKey
import brava.fightinwords.gameplay.wordlookup.forEachWord
import brava.fightinwords.ui.PreviewHelpers
import brava.fightinwords.ui.PreviewHelpers.deez
import brava.fightinwords.ui.PreviewHelpers.nuts
import brava.fightinwords.ui.theme.GameIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinitionBox(
    definedWord: FocusedWord?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    onClick: () -> Unit = {},
    onWordClick: (WordKey) -> Unit = {},
    linkStyles: TextLinkStyles = MaterialTheme.defaultLinkStyles,
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
                    modifier = Modifier.padding(contentPadding),
                    onWordClick = onWordClick,
                    wordLinkStyle = linkStyles
                )
            }
        }
    }
}

@Composable
fun DefinitionView(
    definedWord: FocusedWord,
    modifier: Modifier = Modifier,
    wordStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyLarge
        .copy(fontStyle = FontStyle.Italic),
    definitionStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    wordLinkStyle: TextLinkStyles,
    onWordClick: (WordKey) -> Unit,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefinitionHeadline(definedWord, wordStyle, subtitleStyle)

            if (definedWord.category == WordCategory.Bonus) {
                BonusWordIcon(Modifier.align(Alignment.TopEnd))
            }
        }

        Text(
            text = buildAnnotatedDefinition(
                definedWord.wordDefinition,
                onWordClick,
                wordLinkStyle
            ),
            style = definitionStyle,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun DefinitionHeadline(
    definedWord: FocusedWord,
    wordStyle: TextStyle,
    subtitleStyle: TextStyle,
) {
    Text(
        text = buildAnnotatedString {
            this.appendWord(definedWord.wordDefinition.word)
                .append(' ')

            val parts = sequence {
                val partOfSpeech = definedWord.wordDefinition.partOfSpeech?.toString()?.lowercase()
                if (partOfSpeech != null) {
                    yield(partOfSpeech)
                }

                if (definedWord.points != null) {
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

class WordDefinitionPreviews : PreviewParameterProvider<FocusedWord> {
    override val values: Sequence<FocusedWord>
        get() = sequenceOf(
            FocusedWord(deez, WordCategory.Bonus, 99),
            FocusedWord(nuts, WordCategory.Core, 25)
        )
}

@Preview(showBackground = true)
@Composable
fun DefinitionViewPreview(
    @PreviewParameter(WordDefinitionPreviews::class) definedWordState: FocusedWord,
) {
    Column {
    DefinitionBox(
        definedWordState
    )

        val nwl = PreviewHelpers.naspaWordList

        nwl.forEachWord {
            DefinitionBox(
                FocusedWord(
                    nwl.requireDefinition(it),
                    WordCategory.Bonus,
                    99
                )
            )
        }
    }
}