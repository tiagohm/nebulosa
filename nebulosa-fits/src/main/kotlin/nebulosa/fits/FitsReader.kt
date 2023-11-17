package nebulosa.fits

import java.nio.channels.SeekableByteChannel

interface FitsReader {

    fun read(source: SeekableByteChannel): Hdu<*>?
}
