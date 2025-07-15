package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.util.fastJoinToString
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.PreviewHelpers.deez

@Composable
fun DefinitionView(
    wordDefinition: WordDefinition,
    score: Int? = null,
    wordStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyLarge
        .copy(fontStyle = FontStyle.Italic),
    definitionStyle: TextStyle = MaterialTheme.typography.titleLarge,
    definitionIndent: TextUnit = 2.em
) {

    Column() {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = buildAnnotatedString {
                    this.append(wordDefinition.word.toString() + " ")

                    val parts = sequence {
                        if (wordDefinition.partOfSpeech != null) {
                            yield(wordDefinition.partOfSpeech)
                        }

                        if (score != null) {
                            yield("$score points")
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
                    append(wordDefinition.definition)
                }
            },
            style = definitionStyle,
        )
    }
}

class WordDefinitionPreviews : PreviewParameterProvider<Pair<WordDefinition, Int?>?> {

    override val values: Sequence<Pair<WordDefinition, Int?>?>
        get() = sequenceOf(
            deez to 99,
            deez to null
        )
}

@Preview(showBackground = true)
@Composable
fun DefinitionViewPreview(
    @PreviewParameter(WordDefinitionPreviews::class) definitionAndScore: Pair<WordDefinition, Int?>
) {
    val (wordDefinition, score) = definitionAndScore

    DefinitionView(
        wordDefinition,
        score
    )
}