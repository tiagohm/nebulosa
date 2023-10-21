package nebulosa.fits

import okio.BufferedSource

interface FitsElement {

    fun read(source: BufferedSource)
}
