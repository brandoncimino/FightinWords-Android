package brava.fightinwords.ui.submissions

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.window.Dialog
import brava.fightinwords.botlin.blog
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.PreviewHelpers
import brava.fightinwords.ui.PreviewHelpers.deez
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinitionBox(
    definedWord: DefinedWordState?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    onClick: () -> Unit = {}
) {
    var poppedUp = remember { false }

    Card(
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

        if (poppedUp && definedWord != null) {
            Dialog(
                onDismissRequest = { blog(Log.VERBOSE) { "popping down!" }; poppedUp = false }
            ) {
                Card {
                    DefinitionView(
                        definedWord
                    )
                }
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
    definitionStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom
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

        Text(
            text = definedWord.wordDefinition.definition,
            style = definitionStyle,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

class WordDefinitionPreviews : PreviewParameterProvider<DefinedWordState> {

    override val values: Sequence<DefinedWordState>
        get() = sequenceOf(
            Accepted(deez, 99),
            Unplayed(PreviewHelpers.longDefinition),
            Unplayed(
                Json.decodeFromString<WordDefinition>(Json.encodeToString(deez))
            )
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