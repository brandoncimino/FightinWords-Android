package brava.fightinwords.gameplay.data

/**
 * A single glyph that we use for gameplay.
 *
 * This class exists to take the place of [Char] in most scenarios, but to support situations where a letter can't be represented by a single [Char], such as with [Char.lowercase].
 */
data class Letter(val character: Char) {
    init {
        require(character in 'a'..'z', { "${character.describe()} must be a lowercase letter between 'a' and 'z'." })
    }

    companion object {
        fun Char.toLetter(): Letter = Letter(this)
        fun Char.describe(): String =
            "U+${this.code} `$this` ${Character.getName(this.code)} (${this.category}, ${this.isLetter()}, ${this.isWhitespace()})"
    }

    override fun toString(): String {
        return character.toString()
    }
}