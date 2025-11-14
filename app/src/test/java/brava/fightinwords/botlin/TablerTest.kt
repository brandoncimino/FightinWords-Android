package brava.fightinwords.botlin

import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.appendRowSeparatorTo
import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.rowPrefix
import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.rowSuffix
import brava.fightinwords.botlin.Tabler.Companion.appendTable
import brava.fightinwords.botlin.Tabler.Companion.formatTable
import brava.fightinwords.println
import org.junit.Test
import java.time.DayOfWeek

class TablerTest {
    @Test
    fun simpleTable() {
        val table =
                listOf(
                    "one",
                    "two",
                    "three",
                    """four
                        |five""".trimMargin(),
                    """loooooooooooong
                    |short
                    |looooooooooong
                """.trimMargin()
                ).formatTable(
                    { it },
                    String::length,
                    { it.uppercase() },
                    Tabler.Col { it.lowercase() }
                )

        println(table)
    }

    @Test
    fun simplerTable() {
        val table = buildString {
            appendTable(
                listOf("one"),
                listOf(
                    Tabler.Col { it.uppercase() }
                )
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
        listOf(DayOfWeek.MONDAY)
            .formatTable(
                DayOfWeek::ordinal,
                DayOfWeek::toString,
                gridLines = null
            )
        val table = buildString {
            appendTable(
                listOf(DayOfWeek.MONDAY),
                listOf(Tabler.Col { it.toString() }),
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
        gridLines.appendRowSeparatorTo(sb, LineupLocation.Start, colWidths)

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

    @Test
    fun jaggedTable() {
        listOf(
            listOf(0 to 0, 0 to 1),
            listOf(1 to 0),
            listOf(2 to 0, 2 to 1, 2 to 2)
        ).formatTable()
            .println()
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