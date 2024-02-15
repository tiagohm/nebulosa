package nebulosa.fits

import nebulosa.io.writeFloat
import okio.Buffer
import okio.Sink
import java.nio.ByteBuffer

@Suppress("ArrayInDataClass")
data class FloatArrayImageData(
    override val width: Int,
    override val height: Int,
    @JvmField val data: FloatArray,
) : ImageData {

    override val bitpix = Bitpix.FLOAT

    override fun read(block: (ByteBuffer) -> Unit) {
        val strideSizeInBytes = width * bitpix.byteSize
        val stride = ByteBuffer.allocate(strideSizeInBytes)

        repeat(height) {
            val offset = it * width
            stride.clear()
            for (i in offset until offset + width) stride.putFloat(data[i])
            stride.flip()
            block(stride)
        }
    }

    override fun writeTo(sink: Sink): Long {
        return Buffer().use { buffer ->
            var byteCount = 0L

            repeat(height) {
                val offset = it * width
                for (i in offset until offset + width) buffer.writeFloat(data[i])
                byteCount += buffer.readAll(sink)
            }

            byteCount
        }
    }
}
