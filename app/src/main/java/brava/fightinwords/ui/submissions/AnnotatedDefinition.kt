package brava.fightinwords.ui.submissions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import brava.fightinwords.botlin.appendUtf8
import brava.fightinwords.botlin.asciiBytes
import brava.fightinwords.gameplay.data.appendWord
import brava.fightinwords.gameplay.wordlookup.AnnotatedDefinitionPart
import brava.fightinwords.gameplay.wordlookup.DefinitionLookup.Companion.requireDefinition
import brava.fightinwords.gameplay.wordlookup.NaspaWordList
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.gameplay.wordlookup.WordKey
import brava.fightinwords.gameplay.wordlookup.forEachWord

fun buildAnnotatedDefinition(
    wordDefinition: WordDefinition,
    onWordClick: (WordKey) -> Unit,
    linkStyles: TextLinkStyles,
): AnnotatedString {
    return buildAnnotatedString {
        appendWordDefinition(wordDefinition, onWordClick, linkStyles)
    }
}

fun AnnotatedString.Builder.appendWordDefinition(
    wordDefinition: WordDefinition,
    onWordClick: (WordKey) -> Unit,
    linkStyles: TextLinkStyles,
): AnnotatedString.Builder {
    if (wordDefinition.annotatedParts.isEmpty()) {
        append(wordDefinition.definition)
        return this
    }

    for (part in wordDefinition.annotatedParts) {
        when (part) {
            is AnnotatedDefinitionPart.Literal -> {
                appendUtf8(part.text)
            }

            is AnnotatedDefinitionPart.Link    -> {
                appendWordLink(
                    part.wordKey,
                    onWordClick,
                    linkStyles
                )
            }

            is AnnotatedDefinitionPart.Inline  -> {
                appendWordDefinition(
                    part.inlineDefinition,
                    onWordClick,
                    linkStyles
                )
            }
        }
    }

    return this
}

fun AnnotatedString.Builder.appendWordLink(
    wordKey: WordKey,
    onWordClick: (WordKey) -> Unit,
    linkStyles: TextLinkStyles,
) {
    this.withLink(
        LinkAnnotation.Clickable(
            tag = wordKey.word.toString(),
            linkInteractionListener = {
                onWordClick(wordKey)
            },
            styles = linkStyles
        )
    ) {
        appendWord(wordKey.word)
    }
}

fun <T : Appendable> T.appendCodePoint(codePoint: Int): T {
    if (Character.isBmpCodePoint(codePoint)) {
        append(codePoint.toChar())
        return this
    }

    // ⚠️ WARNING: the "high surrogate" comes FIRST!
    this
        .append(Character.highSurrogate(codePoint))
        .append(Character.lowSurrogate(codePoint))

    return this
}

val MaterialTheme.defaultLinkStyles: TextLinkStyles
    @Composable
    @ReadOnlyComposable
    get() = TextLinkStyles(
        style = SpanStyle(
            color = colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

@Composable
@Preview
fun NaspaDefinitionPreview() {
    val naspaWordListContent = """
        EAT to consume food [v ATE, EATEN, EATEN, EATING, EATS, ET] : EATER [n]
        CHOW to {eat=v} [v CHOWED, CHOWING, CHOWS]
        EATS <eat=v> [v]
        EATER one that {eats=v} [n EATERS]
        EXTRA I don't have any substitutions [n]
    """.trimIndent()

    val nwl = NaspaWordList(naspaWordListContent.asciiBytes())

    Column {
        nwl.forEachWord {
            Text(
                buildAnnotatedString {
                    appendWordDefinition(
                        nwl.requireDefinition(it),
                        { },
                        MaterialTheme.defaultLinkStyles
                    )
                }
            )
        }
    }
}