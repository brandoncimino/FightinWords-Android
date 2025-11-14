package brava.fightinwords.botlin

import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.appendRowSeparatorTo
import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.rowPrefix
import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.rowSuffix
import brava.fightinwords.botlin.Tabler.Companion.appendTable
import org.junit.Test
import java.time.DayOfWeek

class TablerTest {
    @Test
    fun simpleTable() {
        val table = buildString {
            appendTable(
                listOf(
                    "one", "two", "three", "four\nfive", """loooooooooooong
                    |short
                    |looooooooooong
                """.trimMargin()
                ),
                listOf(
                    { it },
                    String::length,
                    { it.uppercase() }
                )
            )
        }

        println(table)
    }

    @Test
    fun simplerTable() {
        val table = buildString {
            appendTable(
                listOf("one"),
                listOf(String::uppercase)
            )
        }

        println(table)
    }

    /**
     * Things I have made by accident:
     * ```
     * ╭───╮
     * │ONE│
     * │   │
     * ╯───╰
     * ```
     *
     */
    @Test
    fun simplestTable() {
        val table = buildString {
            appendTable(
                listOf(DayOfWeek.MONDAY),
                listOf(DayOfWeek::toString),
                gridLines = null
            )
        }

        println("table = ${table}")
    }

    @Test
    fun appendPieces() {
        val gridLines = BoxDrawingCharacters.Rounded
        val colWidths = listOf(2, 3)
        val sb = StringBuilder()
//            .apply {
        gridLines.appendRowSeparatorTo(sb, LineupLocation.Start, colWidths)
//        }

        sb.appendJoin(
            colWidths,
            { append("($it)") },
            { append("+") },
            { append(">>").append(gridLines.rowPrefix(LineupLocation.Start)) },
            { append(gridLines.rowSuffix(LineupLocation.Start)).append("<<") }
        )
            .appendLine()

        sb.printFenced()

        gridLines.appendRowSeparatorTo(sb, LineupLocation.Start, colWidths)


//        sb.append(gridLines.rowPrefix(rowLocation = LineupLocation.Start))
        sb.printFenced()

//        gridLines.appendRowSeparatorTo(sb, LineupLocation.Inner, colWidths)


        sb.appendJoin(
            1..3,
            { append(it) },
            { append('-') },
            { append('[') },
            { append(']') }
        )

        sb.appendLine()

        println(sb)
    }
}

fun <T> T.printFenced(
    header: CharSequence? = null,
    prefix: CharSequence = "```",
    suffix: CharSequence = "```",
): T {
    val lines = sequenceOf(
        header,
        prefix,
        this.toString(),
        suffix
    )
        .filter { it?.isNotBlank() == true }
        .joinToString(separator = "\n", prefix = "\n", postfix = "\n") { it.toString() }

    println(
        lines
    )

    return this
}