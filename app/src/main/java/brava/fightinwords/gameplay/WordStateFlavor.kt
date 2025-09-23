package brava.fightinwords.gameplay

/**
 * Enumerates the kinds of [WordState].
 */
enum class WordStateFlavor(val id: Int) {
    Unplayed('u'.code),
    Rejected('r'.code),
    Accepted('a'.code);

    companion object {
        fun fromId(id: Int) = when (id) {
            Unplayed.id -> Unplayed
            Rejected.id -> Rejected
            Accepted.id -> Accepted
            else        -> throw IllegalArgumentException()
        }

        val WordState.flavor
            get() = when (this) {
                is Accepted -> Accepted
                is Unplayed -> Unplayed
                is Rejected -> Rejected
            }
    }
}