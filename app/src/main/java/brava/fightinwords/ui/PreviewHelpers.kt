package brava.fightinwords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import brava.fightinwords.botlin.asciiBytes
import brava.fightinwords.botlin.utf8Bytes
import brava.fightinwords.gameplay.FocusLens
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.Slug
import brava.fightinwords.gameplay.WordCategory
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.gameplay.scoring.FilterState
import brava.fightinwords.gameplay.scoring.FocusedWord
import brava.fightinwords.gameplay.scoring.ScoreboardWord
import brava.fightinwords.gameplay.scoring.ScoreboardWordVisibility
import brava.fightinwords.gameplay.scoring.WordFilter
import brava.fightinwords.gameplay.wordlookup.AnnotatedDefinitionPart
import brava.fightinwords.gameplay.wordlookup.KnownPartOfSpeech
import brava.fightinwords.gameplay.wordlookup.NaspaWordList
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.gameplay.wordlookup.WordKey
import brava.fightinwords.gameplay.wordlookup.WordSource
import brava.fightinwords.ui.typesetter.TypesetterState

object PreviewHelpers {
    val deez = WordDefinition(
        "deez".toWord(),
        KnownLanguage.English,
        KnownPartOfSpeech.Determiner,
        "(humorous) Pronunciation spelling of these.",
        WordSource.DefinitionsCsv,
        listOf(
            AnnotatedDefinitionPart.Literal("(humorous) Pronunciation spelling of ".utf8Bytes()),
            AnnotatedDefinitionPart.Link(WordKey("these".toWord())),
            AnnotatedDefinitionPart.Literal(".".utf8Bytes())
        )
    )

    val nuts = WordDefinition(
        "nuts".toWord(),
        KnownLanguage.English,
        KnownPartOfSpeech.Noun,
        "Plural of nut.",
        WordSource.NaspaWordList2023,
        listOf(
            AnnotatedDefinitionPart.Literal("Plural of ".utf8Bytes()),
            AnnotatedDefinitionPart.Link(WordKey("nut".toWord())),
            AnnotatedDefinitionPart.Literal(".".utf8Bytes())
        )
    )

    val naspaWordList = NaspaWordList(
        """
        EAT to consume food [v ATE, EATEN, EATEN, EATING, EATS, ET] : EATER [n]
        CHOW to {eat=v} [v CHOWED, CHOWING, CHOWS]
        EATS <eat=v> [v]
        EATER one that {eats=v} [n EATERS]
        EXTRA I don't have any substitutions [n]
    """.trimIndent().asciiBytes()
    )

    val longDefinition =
        deez.copy(definition = "This is a really long version of the original definition for 'deez', which was not quite this long, but now is longer than it once was.")

    val redactedDefinition =
        longDefinition.copy(
            definition = longDefinition.definition.replace(
                Regex("[a-zA-Z]"),
                "█"
            )
        )

    @Composable
    fun Log(
        lines: Iterable<Any?>,
        verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
        nullPlaceholder: String = "⛔",
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = verticalArrangement
        ) {
            lines.forEachIndexed { index, line ->
                Text(
                    line?.toString() ?: nullPlaceholder,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.background(
                        when (index % 2) {
                            0 -> Color.LightGray
                            else -> Color.Gray
                        }
                    )
                )
            }
        }
    }

    @Composable
    fun Log(
        vararg lines: Any?,
        verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
        nullPlaceholder: String = "⛔",
    ) {
        Log(lines.asIterable(), verticalArrangement, nullPlaceholder)
    }

    val gameScreenInteractions = GameScreenInteractions(
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {}
    )

    fun ledgermanUiState(
        wordFilterStates: List<WordFilter.State> = wordFilterStates(),
        visibleWords: List<ScoreboardWord> = obtuseSubmissions(),
        focusedWord: FocusLens.State<FocusedWord>? = FocusLens.State(
            FocusedWord(deez, WordCategory.Bonus, 99)
        ),
    ): LedgermanUiState {
        return LedgermanUiState(
            wordFilterStates,
            visibleWords,
            focusedWord
        )
    }

    fun wordFilterStates(wordLengthRange: IntRange = 3..7): List<WordFilter.State> {
        return wordLengthRange
            .mapIndexed { index, wordLength ->
                WordFilter.State(
                    WordFilter.LengthFilter(wordLength),
                    FilterState.entries[index % FilterState.entries.size]
                )
            }
    }
}

internal fun obtuseSubmissions(): List<ScoreboardWord> {
    return sequenceOf(
//        "obtuse",
        "bet",
        "bot",
        "beot",
        "best",
        "bets",
        "boet",
        "bose",
        "bost",
        "bote",
        "bots",
        "bout",
        "bust",
        "bute",
        "buts",
        "obes",
        "otsu",
        "ouse",
        "oust",
        "oute",
        "outs",
        "sout",
        "stob",
        "stub",
        "suet",
        "tobe",
        "toes",
        "tose",
        "tube",
        "tubs",
        "tues",
        "utes",
        "beots",
        "besot",
        "boets",
        "botes",
        "bouse",
        "bouts",
        "busto",
        "buteo",
        "stube",
        "tobes",
        "touse",
        "tsubo",
        "tubes",
        "buteos",
        "obtuse",
    )
        .map { it ->
            val visibility = when (it.hashCode() % 2) {
                0    -> ScoreboardWordVisibility.Full
                else -> ScoreboardWordVisibility.Masked
            }

            ScoreboardWord(it.toWord(), visibility, WordCategory.Core)
        }
        .toList()
}

internal fun swaggins(): TypesetterState {
    var galleyIndex = 0;
    val letterButtonStates = listOf(
        's' to true,
        'w' to false,
        'a' to false,
        'g' to false,
        'g' to true,
        'i' to true,
        'n' to true,
        's' to false,
        't' to false,
        'a' to false,
        'c' to false
    ).map { (letter, isSlotted) ->
        Slug.State(
            letter.toLetter(), when (isSlotted) {
                true -> galleyIndex++
                false -> -1
            }
        )
    }
    return TypesetterState(
        letterButtonStates
            .filter { it.isSlotted() }
            .map { it.letter }
            .toWord(),
        letterButtonStates
    )
}