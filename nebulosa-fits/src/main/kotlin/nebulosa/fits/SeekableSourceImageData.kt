package nebulosa.fits

import nebulosa.io.Seekable
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

        synchronized(source) {
            source.seek(position)

            repeat(height) {
                buffer.clear()
                require(source.read(buffer, strideSizeInBytes) == strideSizeInBytes) { "unexpected end of file" }
                val data = buffer.readByteArray()
                block(ByteBuffer.wrap(data))
            }
        }
    }
}
