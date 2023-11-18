package nebulosa.fits

import nebulosa.io.SeekableSource

interface FitsReader {

    fun read(source: SeekableSource): Hdu<*>?
}
