package nebulosa.fits

import nebulosa.io.SeekableSource

interface FitsElement {

    fun read(source: SeekableSource)
}
