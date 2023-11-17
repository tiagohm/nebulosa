package nebulosa.fits

import okio.Sink
import java.io.IOException
import java.nio.channels.SeekableByteChannel

object FitsIO : FitsReader, FitsWriter {

    override fun read(source: SeekableByteChannel): Hdu<*> {
        val header = Header.from(source)

        val hdu = when {
            ImageHdu.isValid(header) -> ImageHdu(header)
            else -> throw IOException("invalid FITS format")
        }

        hdu.read(source)

        return hdu
    }

    override fun write(sink: Sink, hdu: Hdu<*>) {
        TODO("Not yet implemented")
    }
}
