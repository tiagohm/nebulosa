package nebulosa.fits

import okio.BufferedSource
import java.nio.ByteBuffer

class ImageHdu : BasicHdu() {

    override fun readData(source: BufferedSource): ByteBuffer {
        val width = header.getInt(Standard.NAXIS1, 0)
        val height = header.getInt(Standard.NAXIS2, 0)
        val count = header.getInt(Standard.NAXIS3, 1)
        val data = ByteArray(width * height * count)
        source.read(data)
        return ByteBuffer.wrap(data)
    }

    companion object {

        @JvmStatic
        fun isValid(header: Header) = header.getBoolean(Standard.SIMPLE)
    }
}
