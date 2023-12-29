package nebulosa.api.atlas

import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.gzip
import java.io.Closeable

class SimbadIdentifierReader(source: Source) : Iterator<SimbadIdentifierEntity>, Closeable {

    private val buffer = if (source is BufferedSource) source else source.gzip().buffer()

    override fun hasNext() = !buffer.exhausted()

    override fun next(): SimbadIdentifierEntity {
        val id = buffer.readLong()
        val byteCount = buffer.readByte().toLong() and 0xFF
        val name = buffer.readString(byteCount, Charsets.UTF_8)
        return SimbadIdentifierEntity(0L, id, name)
    }

    override fun close() {
        buffer.close()
    }
}
