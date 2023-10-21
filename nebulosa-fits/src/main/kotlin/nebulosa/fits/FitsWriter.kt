package nebulosa.fits

import okio.Sink

interface FitsWriter {

    fun write(sink: Sink, hdu: Hdu)
}
