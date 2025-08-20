package brava.fightinwords.botlin

import java.nio.ByteBuffer

private const val lf = '\n'.code.toByte()

internal inline fun ByteBuffer.forEachLine(
    action: (ByteBuffer) -> Unit,
) {
    forEachLineRange { start, endInclusive ->
        val line = this.slice(start, (endInclusive - start) + 1)
        action(line)
    }
}

inline fun ByteBuffer.forEachLineRange(
    action: (start: Int, endInclusive: Int) -> Unit,
) {
    var lineStart = 0
    var pos = 0
    while (pos < limit()) {
        val current = get(pos)
        if (current == lf) {
            action(lineStart, pos - 1)
            lineStart = pos + 1
        }

        pos += 1
    }

    action(lineStart, limit() - 1)
}