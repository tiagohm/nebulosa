package nebulosa.fits

import okio.Sink
import java.nio.ByteBuffer

interface ImageData {

    val width: Int

    val height: Int

    val bitpix: Bitpix

    fun read(block: (ByteBuffer) -> Unit)

    fun writeTo(sink: Sink): Long
}
