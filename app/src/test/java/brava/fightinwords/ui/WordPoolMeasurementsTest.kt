package brava.fightinwords.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import brava.fightinwords.gameplay.Unplayed
import brava.fightinwords.gameplay.wordlookup.WordDefinition
import brava.fightinwords.ui.submissions.WordPoolMeasurements
import brava.fightinwords.ui.submissions.defaultWordPadding
import brava.fightinwords.ui.typesetter.LetterTile
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.Test

class WordPoolMeasurementsTest {
    val saigasJson = """
        [
          {
            "word": [
              "a",
              "g",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "Alternative spelling of agha",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "g",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of aga",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "g",
              "a",
              "s",
              "s",
              "i"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(Korea) Miss, a young Korean, usually unmarried woman. Used as a form of address",
            "isNaspaWord": false
          },
          {
            "word": [
              "a",
              "g",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of ag",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "i",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "Alternative spelling of ayah",
            "isNaspaWord": false
          },
          {
            "word": [
              "a",
              "i",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of aia",
            "isNaspaWord": false
          },
          {
            "word": [
              "a",
              "i",
              "g",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "A Samoan extended family.",
            "isNaspaWord": false
          },
          {
            "word": [
              "a",
              "i",
              "g",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of aiga",
            "isNaspaWord": false
          },
          {
            "word": [
              "a",
              "i",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of ai",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "s",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "Any of several species of horse-like animals, especially Equus asinus, the domesticated of which are used as beasts of burden.",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "s",
              "s",
              "a",
              "i"
            ],
            "language": "English",
            "partOfSpeech": "Adverb",
            "definition": "(music) A tempo direction equivalent to \"very\".",
            "isNaspaWord": true
          },
          {
            "word": [
              "a",
              "s",
              "s",
              "i",
              "g"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(possibly archaic) Abbreviation of assignation or assignee.",
            "isNaspaWord": false
          },
          {
            "word": [
              "g",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(uncountable, physical chemistry) Matter in an intermediate state between liquid and plasma that can be contained only if it is fully surrounded by a solid (or in a bubble of liquid, or held together by gravitational pull); it can condense into a liquid, or can (rarely) become a solid directly by deposition. (uncountable) A flammable gaseous hydrocarbon or hydrocarbon mixture used as a fuel, e.g. for cooking, heating, electricity generation or as a fuel in internal combustion engines in vehicles, especially natural gas.",
            "isNaspaWord": true
          },
          {
            "word": [
              "g",
              "i",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(ornithology, slang) Alternative spelling of jizz",
            "isNaspaWord": true
          },
          {
            "word": [
              "g",
              "i",
              "s",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Verb",
            "definition": "Pronunciation spelling of guess.",
            "isNaspaWord": false
          },
          {
            "word": [
              "g",
              "i",
              "s",
              "s",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Contraction",
            "definition": "(UK, slang, nonstandard, in imperative utterances) Give us a; give me a.",
            "isNaspaWord": false
          },
          {
            "word": [
              "i",
              "g",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Verb",
            "definition": "third-person singular simple present indicative of ig",
            "isNaspaWord": false
          },
          {
            "word": [
              "i",
              "s",
              "s",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Contraction",
            "definition": "Contraction of it's a.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "a",
              "a",
              "g"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "An Indian dish made from greens (usually spinach) cooked down to a thick paste.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "a",
              "a",
              "g",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of saag",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "a",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(India) A mother-in-law, especially the mother of the groom, who lives with the married couple.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "a",
              "g"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "The state of sinking or bending; a droop.",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "g",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "An Old Norse (Icelandic) prose narrative, especially one dealing with family or social histories and legends.",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "g",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of saga",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "g",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of sag",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "i"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "A handheld weapon with three prongs, used in some Oriental martial arts.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "a",
              "i",
              "g",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "Saiga tatarica, an antelope which inhabits a vast area between Kalmykia, Kazakhstan, southern Siberia.",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "i",
              "g",
              "a",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of saiga",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "a",
              "i",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(chiefly India) A groom, or servant with responsibility for the horses.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "i",
              "a"
            ],
            "language": "English",
            "partOfSpeech": "Particle",
            "definition": "(Singlish, Manglish) Tagged at the end of a sentence to express discontent, shock, exhaustion or exasperation.",
            "isNaspaWord": false
          },
          {
            "word": [
              "s",
              "i",
              "g"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(Internet, informal) A signature, especially one on emails or newsgroup postings.",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "i",
              "g",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "plural of sig",
            "isNaspaWord": true
          },
          {
            "word": [
              "s",
              "i",
              "s"
            ],
            "language": "English",
            "partOfSpeech": "Noun",
            "definition": "(informal) Clipping of sister.",
            "isNaspaWord": true
          }
        ]
    """

    val saigasWords = Json.decodeFromString<List<WordDefinition>>(saigasJson)
        .map { Unplayed(it) }
        .toList();

    @Test
    fun fancyTest() {
        val result = WordPoolMeasurements.shrinkLettersToFit(
            maxLetterPersonalSpace = LetterTile.Big,
            minLetterPersonalSpace = LetterTile.Small,
            wordPadding = defaultWordPadding,
            wordLengths = saigasWords.map { it.word.length },
            availableSpace = DpSize(485.39322.dp, 398.65167.dp),
            bigShrinker = { it - 10.dp },
            littleGrower = { it + 3.dp }
        )

        Assertions.assertThat(result)
            .satisfies(
                { Assertions.assertThat(it.fits).isTrue },
                { Assertions.assertThat(it.letterPersonalSpace).isGreaterThan(LetterTile.Small) }
            );
    }
}