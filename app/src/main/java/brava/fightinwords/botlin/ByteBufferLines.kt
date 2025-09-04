package brava.fightinwords.botlin

import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
        if (current == '\n'.code.toByte()) {
            action(lineStart, pos - 1)
            lineStart = pos + 1
        }

        pos += 1
    }

    action(lineStart, limit() - 1)
}