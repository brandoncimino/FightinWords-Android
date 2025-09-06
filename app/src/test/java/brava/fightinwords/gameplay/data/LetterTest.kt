package brava.fightinwords.gameplay.data

import org.assertj.core.api.Assertions
import org.junit.Test

class LetterTest {
    @Test
    fun azBecomesTiny(){
        val upperAZ = 'A'..'Z'
        val lowerAZ = 'a'..'z'
        val allAZ = buildList {
            addAll(upperAZ)
            addAll(lowerAZ)
        }

        Assertions.assertThat(allAZ)
            .allSatisfy { azBecomesTiny(it) }
    }

    fun azBecomesTiny(az: Char){
        require(az in 'a'..'z' || az in 'A'..'Z')

        Assertions.assertThat(az)
            .satisfies(
                {
                    Assertions.assertThat(Letter.of(it))
                        .describedAs { "of char" }
                        .isInstanceOf(TinyLetter::class.java)
                },
                {
                    Assertions.assertThat(Letter.of(it.code.toByte()))
                        .describedAs { "of byte" }
                        .isInstanceOf(TinyLetter::class.java)
                },
                {
                    Assertions.assertThat(Letter.of(it.code))
                        .describedAs { "of int" }
                        .isInstanceOf(TinyLetter::class.java)
                },
            )
    }
}