package brava.fightinwords.gameplay

/**
 * A physical [brava.fightinwords.Letter] object that can be moved around by a [Typesetter].
 *
 * > It might be more accurate to call this a [sort](https://en.wikipedia.org/wiki/Sort_(typesetting)), but that is needlessly confusing in the context of programming.
 */
class Slug(val letter: Char, val myTypesetter: Typesetter) {
    val isSlotted: Boolean
        get() {
            return myTypesetter.galley.contains(this)
        }

    override fun toString(): String {
        return "🔲$letter"
    }
}