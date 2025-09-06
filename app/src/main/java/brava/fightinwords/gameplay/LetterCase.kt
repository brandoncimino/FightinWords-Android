package brava.fightinwords.gameplay

import brava.fightinwords.gameplay.data.Letter
import java.util.*


enum class LetterCase {
    Lowercase,
    Uppercase;

    fun applyTo(letter: Letter, locale: Locale = Locale.ROOT): String {
        return applyTo(letter.codePoint, locale)
    }

    fun applyTo(string: String, locale: Locale = Locale.ROOT): String {
        return when (this) {
            Uppercase -> string.uppercase(locale)
            Lowercase -> string.lowercase(locale)
        }
    }

    fun applyTo(char: Char, locale: Locale = Locale.ROOT): String {
        return when (this) {
            Uppercase -> char.uppercase(locale)
            Lowercase -> char.lowercase()
        }
    }

    fun applyTo(codePoint: Int, locale: Locale = Locale.ROOT) : String {
        return when(this){
            Uppercase -> StringBuilder().appendCodePoint(codePoint).toString().uppercase(locale)
            Lowercase -> StringBuilder().appendCodePoint(codePoint).toString().lowercase(locale)
        }
    }
}