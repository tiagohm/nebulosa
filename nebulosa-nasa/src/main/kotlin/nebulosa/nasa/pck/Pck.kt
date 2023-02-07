package nebulosa.nasa.pck

import nebulosa.nasa.daf.Daf
import nebulosa.nasa.daf.Summary
import java.io.Closeable
import java.io.IOException

class Pck(private val daf: Daf) : Closeable by daf {

    init {
        daf.read()
    }

    private val segments by lazy {
        daf.summaries
            .map { it.segment(daf) }
            .associateBy { it.body }
    }

    companion object {

        private fun Summary.segment(daf: Daf): PckSegment {
            val start = doubleAt(0)
            val end = doubleAt(1)
            val body = intAt(0)
            val frame = intAt(1)
            val type = intAt(2)
            val startIndex = intAt(3)
            val endIndex = intAt(4)

            return when (type) {
                2 -> Type2Segment(daf, name, start, end, body, frame, type, startIndex, endIndex)
                else -> throw IOException("only binary PCK data type 2 is supported")
            }
        }
    }
}
