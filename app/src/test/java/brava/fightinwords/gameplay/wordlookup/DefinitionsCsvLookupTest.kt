package brava.fightinwords.gameplay.wordlookup

import brava.fightinwords.botlin.fastSlice
import brava.fightinwords.botlin.getMemoryMappedBuffer
import brava.fightinwords.gameplay.KnownLanguage
import brava.fightinwords.gameplay.data.Word.Companion.toWord
import org.assertj.core.api.Assertions
import org.junit.Test
import java.nio.file.Files

class DefinitionsCsvLookupTest {
    val firstFewLines: String = """
        aaagh,Interjection,0,Elongated form of agh.
        aaargh,Interjection,0,Alternative form of argh!
        aaawesome,Adjective,0,Elongated form of awesome.
        aabomycin,Noun,0,venturicidin
        aabomycins,Noun,0,plural of aabomycin
        aad,Adjective,0,(Geordie) old
        aads,Noun,0,plural of aad
        aagh,Interjection,0,An exclamation of horror, disgust or frustration
        aah,Interjection,1,Indication of amazement or surprise or enthusiasm.
        aahed,Verb,1,simple past and past participle of aah
        aaher,Noun,0,One who aahs (in various senses).
        aahing,Verb,1,present participle and gerund of aah
        aahings,Noun,0,plural of aahing
        aahs,Noun,1,plural of aah
    """.trimIndent()

    @Test
    fun fromDefinitionsCsvFile() {
        val tempFile = Files.createTempFile(null,null)
        Files.write(tempFile, firstFewLines.lines())
        val lookup = DefinitionsCsvLookup(tempFile.toFile().getMemoryMappedBuffer().fastSlice())

        val word = "aabomycins".toWord()
        val found = lookup.findDefinition(word)
        println("found = ${found}")

        Assertions.assertThat(found)
            .isEqualTo(
                WordDefinition(
                    word,
                    KnownLanguage.English,
                    "Noun",
                    "plural of aabomycin",
                    WordSource.DefinitionsCsv
                )
            )
    }
}