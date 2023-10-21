package nebulosa.fits

import okio.BufferedSource
import java.nio.ByteBuffer

abstract class BasicHdu : Hdu {

    val header = Header()

    protected abstract fun readData(source: BufferedSource): ByteBuffer

    override fun read(source: BufferedSource) {
        header.read(source)
        readData(source)
    }
}
