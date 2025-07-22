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
import brava.fightinwords.gameplay.Accepted
import brava.fightinwords.gameplay.DefinedWordState
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.data.Letter.Companion.toLetter
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.typesetter.LetterButtonState
import brava.fightinwords.ui.typesetter.TypesetterState

object PreviewHelpers {
    val deez = WordDefinition(
        "deez".toWord(),
        KnownLanguage.English,
        "determiner",
        "(humorous) Pronunciation spelling of these.",
        false
    )

    val longDefinition =
        deez.copy(definition = "This is a really long version of the original definition for 'deez', which was not quite this long, but now is longer than it once was.")

    @Composable
    fun Log(
        lines: Iterable<Any?>,
        verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
        nullPlaceholder: String = "⛔"
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
        nullPlaceholder: String = "⛔"
    ) {
        Log(lines.asIterable(), verticalArrangement, nullPlaceholder)
    }
}

internal fun obtuseSubmissions(padWordsToLength: Int? = null): List<DefinedWordState> {
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
        .map {
            val word = it.padEnd(padWordsToLength ?: 0, 'z').toWord()
            val accepted = it.hashCode() % 2 == 0
            val wordDefinition = PreviewHelpers.deez.copy(word = word, isNaspaWord = it.hashCode() % 5 != 0)
            when {
                accepted -> Accepted(wordDefinition, it.hashCode())
                else -> Unplayed(wordDefinition)
            }
        }
        .toList()
}

internal fun swaggins(): TypesetterState {
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
    ).map { (letter, isSlotted) -> LetterButtonState(letter.toLetter(), isSlotted) }
    return TypesetterState(
        letterButtonStates
            .filter { it.isSlotted }
            .map { it.letter }
            .toWord(),
        letterButtonStates
    )
}