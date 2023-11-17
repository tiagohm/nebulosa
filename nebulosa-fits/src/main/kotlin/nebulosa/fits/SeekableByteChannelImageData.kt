package nebulosa.fits

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SeekableByteChannel

data class SeekableByteChannelImageData(
    private val source: SeekableByteChannel,
    private val position: Long,
    override val width: Int,
    override val height: Int,
    override val bitpix: Bitpix,
) : ImageData {

    override fun read(block: (ByteBuffer) -> Unit) {
        val strideSizeInBytes = width * bitpix.byteSize

        val buffer = ByteBuffer.allocate(strideSizeInBytes)!!
        buffer.order(ByteOrder.BIG_ENDIAN)

        synchronized(source) {
            source.position(position)

            repeat(height) {
                buffer.clear()
                require(source.read(buffer) == strideSizeInBytes) { "unexpected end of file" }
                buffer.flip()
                block(buffer)
            }
        }
    }
}
