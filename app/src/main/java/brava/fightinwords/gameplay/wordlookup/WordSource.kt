package brava.fightinwords.gameplay.wordlookup

/**
 * The place where we got a given [WordDefinition] from.
 */
enum class WordSource {
    DefinitionsCsv,
    NaspaWordList2023,
    ;

    val isNaspa
        inline get() = when (this) {
            NaspaWordList2023 -> true
            else              -> false
        }
}