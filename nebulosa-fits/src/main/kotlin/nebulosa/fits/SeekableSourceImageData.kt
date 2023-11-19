package nebulosa.fits

import nebulosa.io.Seekable
import nebulosa.io.sink
import okio.Buffer
import okio.Source
import java.nio.ByteBuffer

data class SeekableSourceImageData(
    private val source: Seekable,
    private val position: Long,
    override val width: Int,
    override val height: Int,
    override val bitpix: Bitpix,
) : ImageData {

    override fun read(block: (ByteBuffer) -> Unit) {
        require(source is Source)

        val strideSizeInBytes = (width * bitpix.byteSize).toLong()

        val buffer = Buffer()
        val data = ByteArray(strideSizeInBytes.toInt())
        val sink = data.sink()

        synchronized(source) {
            source.seek(position)

            repeat(height) {
                sink.seek(0L)

                buffer.transferFully(source, sink, strideSizeInBytes)
                block(ByteBuffer.wrap(data))

                buffer.clear()
            }
        }
    }
}
