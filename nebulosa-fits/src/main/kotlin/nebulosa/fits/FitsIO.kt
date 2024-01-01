package nebulosa.fits

import nebulosa.io.SeekableSource
import okio.Sink
import java.io.IOException

object FitsIO : FitsReader, FitsWriter {

    override fun read(source: SeekableSource): Hdu<*> {
        val header = Header.from(source)

        val hdu = when {
            ImageHdu.isValid(header) -> ImageHdu(header)
            else -> throw IOException("unsupported FITS header")
        }

        hdu.read(source)

        return hdu
    }

    override fun write(sink: Sink, hdu: Hdu<*>) {
        TODO("Not yet implemented")
    }
}
