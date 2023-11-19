package nebulosa.fits

import java.nio.ByteBuffer

@Suppress("ArrayInDataClass")
data class FloatArrayImageData(
    override val width: Int,
    override val height: Int,
    val data: FloatArray,
) : ImageData {

    override val bitpix = Bitpix.FLOAT

    override fun read(block: (ByteBuffer) -> Unit) {
        val strideSizeInBytes = width * bitpix.byteSize
        val stride = ByteBuffer.allocate(strideSizeInBytes)

        repeat(height) {
            val offset = it * width
            stride.clear()
            for (i in 0 until width) stride.putFloat(data[offset + i])
            stride.flip()
            block(stride)
        }
    }
}
