package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.util.fastJoinToString
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.ui.PreviewHelpers.deez

@Composable
fun DefinitionBox(
    definedWord: DefinedWordState?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    Box(
        modifier = modifier
    ) {
        when (definedWord) {
            null -> Text(" ")
            else -> DefinitionView(
                definedWord,
                modifier = Modifier.padding(contentPadding)
            )
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
    definitionStyle: TextStyle = MaterialTheme.typography.titleLarge,
    definitionIndent: TextUnit = 2.em
) {

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = buildAnnotatedString {
                    this.append(definedWord.word.toString() + " ")

                    val parts = sequence {
                        if (definedWord.wordDefinition.partOfSpeech != null) {
                            yield(definedWord.wordDefinition.partOfSpeech)
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

        Text(
            text = buildAnnotatedString {
                withStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(definitionIndent, definitionIndent)
                    )
                ) {
                    append(definedWord.wordDefinition.definition)
                }
            },
            style = definitionStyle,
        )
    }
}

class WordDefinitionPreviews : PreviewParameterProvider<DefinedWordState> {

    override val values: Sequence<DefinedWordState>
        get() = sequenceOf(
            Accepted(deez, 99),
            Unplayed(deez)
        )
}

@Preview(showBackground = true)
@Composable
fun DefinitionViewPreview(
    @PreviewParameter(WordDefinitionPreviews::class) definedWordState: DefinedWordState
) {
    DefinitionBox(
        definedWordState
    )
}