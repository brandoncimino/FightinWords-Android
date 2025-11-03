package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.AliasMatcher
import brava.fightinwords.botlin.AliasMatcher.Companion.aliasMatcher
import com.google.common.collect.ImmutableList

sealed interface PartOfSpeech {
    companion object {
        fun tryParse(nameOrAbbreviation: CharSequence?): PartOfSpeech? {
            if (nameOrAbbreviation?.isEmpty() ?: true) {
                return null
            }

            val matched = KnownPartOfSpeech.aliasMatcher.tryMatch(nameOrAbbreviation)

            return when (matched) {
                is AliasMatcher.Ambiguous<KnownPartOfSpeech>   -> {
                    // TODO: Some kind of reporting for this
                    UnknownPartOfSpeech(nameOrAbbreviation.toString())
                }

                is AliasMatcher.Unambiguous<KnownPartOfSpeech> -> matched.matchedValue
                null                                           -> {
                    // TODO: Some kind of reporting for this
                    UnknownPartOfSpeech(nameOrAbbreviation.toString())
                }
            }
        }
    }
}

/**
 * From Wikipedia's [part of speech](https://en.wikipedia.org/wiki/Part_of_speech):
 * > Commonly listed English parts of speech are noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection, numeral, article, and determiner.
 */
enum class KnownPartOfSpeech(vararg aliases: String) : PartOfSpeech {
    Noun("n" /* To avoid `n` mapping ambiguously with `Numeral` */),
    Verb,
    Adjective,
    Adverb,

    /**
     * [whomst](https://en.wiktionary.org/wiki/whomst#Pronoun)
     */
    Pronoun,
    Preposition,

    /**
     * [ergo](https://en.wiktionary.org/wiki/ergo#Conjunction),
     * [and](https://en.wiktionary.org/wiki/and#Conjunction)
     */
    Conjunction,

    /**
     * [daggum](https://en.wiktionary.org/wiki/daggum#Interjection),
     * [zounds](https://en.wiktionary.org/wiki/zounds#Interjection)
     */
    Interjection,

    /**
     * [the](https://en.wiktionary.org/wiki/the#Article)
     */
    Article,
    Numeral,

    /**
     * [deez](https://en.wiktionary.org/wiki/deez#Determiner)
     *
     * TODO: Need to investigate how [Determiner]s related to [Pronoun]s:
     *  `these` and `those` are **both** in Wiktionary; is that true for other [Determiner]s?
     */
    Determiner,
    ;

    private val aliases: ImmutableList<String> = ImmutableList.copyOf(aliases)

    companion object {
        val aliasMatcher = entries.aliasMatcher(
            ignoreCase = true,
            aliases = { it.aliases }
        )
    }

}

/**
 * Something that a source told us was a part-of-speech, but we couldn't map to one of the [KnownPartOfSpeech].
 *
 * The two likely scenarios for this are:
 * 1. We got an exotic value, like ["proverb"](https://en.wiktionary.org/wiki/Category:English_proverbs)
 * 2. We didn't properly map the value to the correct [KnownPartOfSpeech]
 *
 * # Examples of exotic parts-of-speech from Wiktionary:
 * - `symbol`: [±](https://en.wiktionary.org/wiki/%C2%B1#Symbol), [😱](https://en.wiktionary.org/wiki/%F0%9F%98%B1)
 * - `phrase`: ["yolo"](https://en.wiktionary.org/wiki/YOLO#Phrase)
 * - `proverb`: ["you only live once"](https://en.wiktionary.org/wiki/you_only_live_once#Proverb)
 * - `romanization`: [Gothic "and"](https://en.wiktionary.org/wiki/and#Romanization), [Japanese _(but not English; that's a noun)_ "waifu"](https://en.wiktionary.org/wiki/waifu#Romanization)
 */
data class UnknownPartOfSpeech(val unknownValue: String) : PartOfSpeech {
    init {
        require(unknownValue.isNotBlank()) { "An ${javaClass.simpleName} cannot be a blank string!" }
    }
}