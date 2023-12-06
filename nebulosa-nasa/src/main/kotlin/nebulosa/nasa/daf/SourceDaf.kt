package nebulosa.nasa.daf

import nebulosa.io.SeekableSource
import nebulosa.io.read
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okio.Buffer
import java.io.Closeable

class SourceDaf(private val source: SeekableSource) : Daf(), Closeable by source {

    override fun read(start: Int, end: Int): DoubleArray {
        source.seek(8L * (start - 1))
        val length = 1 + end - start
        val buffer = Buffer()
        source.read(buffer, length * 8L)
        return buffer.readDoubleArray(length, record.order)
    }

    override fun Buffer.readRecord(index: Int): SeekableSource {
        source.seek((index - 1) * 1024L)
        return read(source, 1024L) { readByteArray().source() }
    }
}
