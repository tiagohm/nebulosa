package nebulosa.nasa.daf

import nebulosa.io.SeekableSource
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okio.Buffer
import java.io.Closeable
import java.io.File

class SourceDaf(private val source: SeekableSource) : Daf(), Closeable by source {

    constructor(file: File) : this(file.source())

    override fun read(start: Int, end: Int): DoubleArray {
        source.seek(8L * (start - 1))
        val length = 1 + end - start
        val buffer = Buffer()
        source.read(buffer, length * 8L)
        return buffer.readDoubleArray(length, record.order)
    }

    override fun readRecord(index: Int): SeekableSource {
        source.seek((index - 1) * 1024L)
        val record = Buffer()
        source.read(record, 1024L)
        return record.readByteArray().source()
    }
}
