package nebulosa.api.atlas

import okio.BufferedSink
import okio.Sink
import okio.buffer
import okio.gzip
import java.io.Closeable

class SimbadIdentifierWriter(sink: Sink) : Closeable {

    private val buffer = if (sink is BufferedSink) sink else sink.gzip().buffer()

    fun write(identifier: SimbadIdentifierEntity) {
        write(identifier.id, identifier.name)
    }

    fun write(id: Long, name: String) {
        buffer.writeLong(id)
        val bytes = name.encodeToByteArray()
        buffer.writeByte(bytes.size)
        buffer.write(bytes)
    }

    override fun close() {
        buffer.flush()
        buffer.close()
    }
}
