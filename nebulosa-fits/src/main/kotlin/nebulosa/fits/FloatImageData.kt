package nebulosa.fits

import nebulosa.io.writeFloat
import okio.Buffer
import okio.Sink
import java.nio.ByteBuffer

@Suppress("ArrayInDataClass")
data class FloatImageData(
    override val width: Int,
    override val height: Int,
    @JvmField val data: FloatArray,
) : ImageData {

    override val bitpix = Bitpix.FLOAT

    override fun read(block: (ByteBuffer) -> Unit) {
        val strideSizeInBytes = width * bitpix.byteSize
        val stride = ByteBuffer.allocate(strideSizeInBytes)

        repeat(height) {
            var offset = it * width
            stride.clear()
            repeat(width) { stride.putFloat(data[offset++]) }
            stride.flip()
            block(stride)
        }
    }

    override fun writeTo(sink: Sink): Long {
        return Buffer().use { b ->
            var byteCount = 0L

            repeat(height) {
                var offset = it * width
                repeat(width) { b.writeFloat(data[offset++]) }
                byteCount += b.readAll(sink)
            }

            byteCount
        }
    }
}
