package brava.fightinwords.botlin

import brava.fightinwords.botlin.BoxDrawingCharacters.Companion.appendRowSeparatorTo
import brava.fightinwords.botlin.BoxDrawingCharacters.Sides.Companion.bottomSplitter
import brava.fightinwords.botlin.BoxDrawingCharacters.Sides.Companion.leftSideSplitter
import brava.fightinwords.botlin.BoxDrawingCharacters.Sides.Companion.rightSideSplitter
import brava.fightinwords.botlin.BoxDrawingCharacters.Sides.Companion.topSplitter
import brava.fightinwords.botlin.LineupLocation.End
import brava.fightinwords.botlin.LineupLocation.Inner
import brava.fightinwords.botlin.LineupLocation.Start
import brava.fightinwords.botlin.RepeatingChar.Companion.repeated

enum class LineupLocation {
    Start,
    Inner,
    End,
}

@Suppress("unused")
data class BoxDrawingCharacters(
    val sides: Sides,
    val corners: Corners,
) {
    companion object {
        val Light = BoxDrawingCharacters(Sides.Light, Corners.Light)
        val Heavy = BoxDrawingCharacters(Sides.Heavy, Corners.Heavy)
        val Rounded = BoxDrawingCharacters(Sides.Light, Corners.Rounded)

        val BoxDrawingCharacters.topOuterSeparator get() = sides.splitters?.top ?: sides.horizontal

        fun BoxDrawingCharacters.rowCellSeparator(
            rowLocation: LineupLocation,
            centerSplitterFallback: Char = ' ',
        ) = when (rowLocation) {
            Start -> sides.topSplitter
            Inner -> sides.splitters?.center ?: centerSplitterFallback
            End   -> sides.bottomSplitter
        }

        fun BoxDrawingCharacters.rowPrefix(rowLocation: LineupLocation) = when (rowLocation) {
            Start -> corners.topLeft
            Inner -> sides.leftSideSplitter
            End   -> corners.bottomLeft
        }

        fun BoxDrawingCharacters.rowSuffix(rowLocation: LineupLocation) = when (rowLocation) {
            Start -> corners.topRight
            Inner -> sides.rightSideSplitter
            End   -> corners.bottomRight
        }

        inline fun Appendable.appendRowSeparator(
            gridLines: BoxDrawingCharacters,
            colCount: Int,
            colWidth: (Int) -> Int,
            firstChar: Char,
            lastChar: Char,
        ) {
            if (colCount <= 0) {
                return
            }

            for (i in 0 until colCount) {
                val left = when (i) {
                    0    -> firstChar
                    else -> gridLines.sides.leftSideSplitter
                }

                append(left)

                repeat(colWidth(i)) {
                    append(gridLines.sides.horizontal)
                }

            }

            append(lastChar)
        }

        fun BoxDrawingCharacters.appendRowSeparatorTo(
            appendable: Appendable,
            rowLocation: LineupLocation,
            colWidths: Iterable<Int>,
        ) {
            appendable.appendJoin(
                stuff = colWidths.asIterable(),
                element = {
//                    append('[')
                    append(sides.horizontal.repeated(it))
//                    append(']')
                },
                prefix = { append(rowPrefix(rowLocation)) },
                separator = { append(rowCellSeparator(rowLocation)) },
                suffix = { append(rowSuffix(rowLocation)) }
            ).appendLine()
        }
    }

    data class Sides(
        val horizontal: Char,
        val vertical: Char,
        val halves: Halves? = null,
        val splitters: Splitters? = null,
    ) {
        data class Halves(
            val up: Char,
            val down: Char,
            val left: Char,
            val right: Char,
        )

        data class Splitters(
            val left: Char,
            val right: Char,
            val top: Char,
            val bottom: Char,
            val center: Char,
        )

        companion object {
            val Sides.topSplitter get() = splitters?.top ?: horizontal
            val Sides.bottomSplitter get() = splitters?.bottom ?: horizontal
            val Sides.leftSideSplitter get() = splitters?.left ?: vertical
            val Sides.rightSideSplitter get() = splitters?.right ?: vertical

            val Light = Sides(
                horizontal = '─',
                vertical = '│',
                halves = Halves(
                    up = '╵',
                    down = '╷',
                    left = '╴',
                    right = '╶'
                ),
                splitters = Splitters(
                    left = '├',
                    right = '┤',
                    top = '┬',
                    bottom = '┴',
                    center = '┼',
                )
            )

            val Heavy = Sides(
                horizontal = '━',
                vertical = '┃',
                halves = Halves(
                    up = '╹',
                    down = '╻',
                    left = '╺',
                    right = '╸',
                ),
                splitters = Splitters(
                    left = '┣',
                    right = '┫',
                    top = '┳',
                    bottom = '┻',
                    center = '╋',
                )
            )

            val Double = Sides(
                horizontal = '═',
                vertical = '║',
                splitters = Splitters(
                    left = '╠',
                    right = '╣',
                    top = '╦',
                    bottom = '╩',
                    center = '╬',
                )
            )

            val LightDoubleDash = Sides('╌', '╎')
            val HeavyDoubleDash = Sides('╍', '╏')
            val LightTripleDash = Sides('┄', '┆')
            val HeavyTripleDash = Sides('┅', '┇')
            val LightQuadrupleDash = Sides('┈', '┊')
            val HeavyQuadrupleDash = Sides('┉', '┋')
        }
    }

    data class Corners(
        val topLeft: Char,
        val topRight: Char,
        val bottomLeft: Char,
        val bottomRight: Char,
    ) {
        companion object {
            val Light = Corners(
                topLeft = '┌',
                topRight = '┐',
                bottomLeft = '└',
                bottomRight = '┘',
            )

            val Heavy = Corners(
                topLeft = '┏',
                topRight = '┓',
                bottomLeft = '┗',
                bottomRight = '┛',
            )

            val Rounded = Corners(
                topLeft = '╭',
                topRight = '╮',
                bottomLeft = '╰',
                bottomRight = '╯',
            )

            val Double = Corners(
                topLeft = '╔',
                topRight = '╗',
                bottomLeft = '╚',
                bottomRight = '╝',
            )
        }
    }
}

class Tabler {
    class Col<ROW, CELL>(
        val header: CharSequence? = null,
        val cellFunction: (ROW) -> CELL,
    ) : (ROW) -> CELL {
        override fun invoke(p1: ROW): CELL {
            return cellFunction(p1)
        }
    }

    companion object {
        enum class HeaderRowPresence {
            IfNotBlank,
            Never,
            Always
        }

        private fun <APPENDABLE : Appendable> APPENDABLE.appendTableRows(
            rowInfos: List<RowInfo>,
            gridLines: BoxDrawingCharacters?,
        ): APPENDABLE {
            val colWidths = rowInfos[0].cells.indices.map { colIndex ->
                rowInfos.maxOf { it.cells[colIndex].width }
            }

            fun RowInfo.getLineSegments(lineIndex: Int): Sequence<CharSequence> {
                return cells.asSequence()
                    .mapIndexed { colIndex, cell ->
                        cell.lines
                            .getOrElse(lineIndex) { "" }
                            .padEnd(colWidths[colIndex])
                    }
            }

            fun appendRowContentLine(row: RowInfo, lineIndex: Int) {
                appendJoin(
                    stuff = row.getLineSegments(lineIndex)
                        .asIterable(),
                    element = { append(it) },
                    separator = {
                        append(
                            gridLines?.sides?.vertical ?: ' '
                        )
                    },
                    prefix = {
                        gridLines?.let { append(it.sides.vertical) }
                    },
                    suffix = {
                        gridLines?.let { append(it.sides.vertical) }
                    }
                )
                    .appendLine()
            }

            fun appendRowContent(row: RowInfo) {
                for (lineIndex in 0 until row.height) {
                    appendRowContentLine(row, lineIndex)
                }
            }

            return this.apply {
                appendJoin(
                    stuff = rowInfos,
                    element = { appendRowContent(it) },
                    separator = {
                        gridLines?.appendRowSeparatorTo(
                            this,
                            Inner,
                            colWidths
                        )
                    },
                    prefix = {
                        appendLine()

                        gridLines?.appendRowSeparatorTo(
                            this,
                            Start,
                            colWidths
                        )
                    },
                    suffix = {

                        gridLines?.appendRowSeparatorTo(
                            this,
                            End,
                            colWidths
                        )
                    }
                )
            }
        }

        fun <APPENDABLE : Appendable, ROW> APPENDABLE.appendTable(
            rows: Iterable<ROW>,
            cols: Iterable<Col<ROW, *>>,
            errorRenderer: (Throwable) -> Any = { it },
            gridLines: BoxDrawingCharacters? = BoxDrawingCharacters.Rounded,
            headerRowPresence: HeaderRowPresence = HeaderRowPresence.IfNotBlank,
        ): APPENDABLE {
            val rowInfos = buildList {
                var rowIndex = 0

                fun MutableList<RowInfo>.addRow(
                    cellFunction: (col: Col<ROW, *>) -> Any?,
                ): Int {
                    val cells = cols.mapIndexed { colIndex, col ->
                        CellInfo(
                            cellFunction(col).toString().lines(),
                            rowIndex,
                            colIndex
                        )
                    }

                    add(RowInfo(cells))
                    return rowIndex + 1
                }

                when (headerRowPresence) {
                    HeaderRowPresence.Never      -> {}
                    HeaderRowPresence.Always     -> rowIndex = addRow { it.header }
                    HeaderRowPresence.IfNotBlank -> {
                        if (cols.any { it.header?.isNotBlank() == true }) {
                            rowIndex = addRow { it.header }
                        }
                    }
                }

                for (row in rows) {
                    rowIndex = addRow {
                        runCatching { it.cellFunction(row) }
                            .getOrElse(errorRenderer)
                    }
                }
            }

            return appendTableRows(
                rowInfos = rowInfos,
                gridLines = gridLines
            )
        }


        fun <ROW, CELL> Iterable<ROW>.formatTable(
            vararg cols: (ROW) -> CELL,
            gridLines: BoxDrawingCharacters? = BoxDrawingCharacters.Rounded,
            errorRenderer: (Throwable) -> Any = { it },
            headerRowPresence: HeaderRowPresence = HeaderRowPresence.IfNotBlank,
        ): String {
            return buildString {
                appendTable(
                    this@formatTable,
                    cols.map { Col(cellFunction = it) },
                    errorRenderer,
                    gridLines,
                    headerRowPresence
                )
            }
        }

        fun <K, V> Map<K, V>.formatTable(): String {
            return entries.formatTable(
                Col { it.key },
                Col { it.value }
            )
        }
    }
}

inline fun <A : Appendable, T> A.appendJoin(
    stuff: Iterable<T>,
    element: A.(T) -> Unit,
    separator: A.() -> Unit,
    prefix: A.() -> Unit = {},
    suffix: A.() -> Unit = {},
): A {
    return stuff.joinTo(this, element, separator, prefix, suffix)
}

inline fun <A : Appendable, T> Iterable<T>.joinTo(
    appendable: A,
    element: A.(T) -> Unit,
    separator: A.() -> Unit,
    prefix: A.() -> Unit = {},
    suffix: A.() -> Unit = {},
): A {
    forEachIndexed { index, t ->
        when (index) {
            0    -> appendable.prefix()
            else -> appendable.separator()
        }

        appendable.element(t)
    }

    appendable.suffix()

    return appendable
}

private data class RepeatingChar(
    val char: Char,
    override val length: Int,
) : CharSequence {
    override fun get(index: Int) = char

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return RepeatingChar(char, endIndex - startIndex)
    }

    companion object {
        fun Char.repeated(count: Int) = RepeatingChar(this, count)
    }
}

private data class CellInfo(
    val lines: List<String>,
    val rowIndex: Int,
    val colIndex: Int,
) {
    val width = lines.maxOf { it.length }
    val height = lines.size
}

private data class RowInfo(val cells: List<CellInfo>) {
    val height = cells.maxOf { it.height }
}