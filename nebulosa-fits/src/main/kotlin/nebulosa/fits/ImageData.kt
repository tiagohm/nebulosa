package nebulosa.fits

import java.nio.ByteBuffer

interface ImageData {

    val width: Int

    val height: Int

    val bitpix: Bitpix

    fun read(block: (ByteBuffer) -> Unit)
}
