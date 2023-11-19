package nebulosa.fits

import okio.Buffer
import okio.Sink
import okio.Source

internal fun Buffer.transferFully(source: Source, sink: Sink, byteCount: Long) {
    var remainingCount = byteCount

    while (remainingCount > 0L) {
        val size = source.read(this, remainingCount)
        require(size > 0) { "unexpected end of file" }
        sink.write(this, size)
        remainingCount -= size
    }
}
