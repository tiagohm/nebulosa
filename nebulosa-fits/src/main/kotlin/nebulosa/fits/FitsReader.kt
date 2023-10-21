package nebulosa.fits

import okio.Source

interface FitsReader {

    fun read(source: Source): Hdu?
}
