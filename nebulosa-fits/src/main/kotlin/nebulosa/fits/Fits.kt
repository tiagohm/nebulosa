package nebulosa.fits

import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer

class Fits : FitsReader, FitsWriter {

    override fun read(source: Source): Hdu? {
        val buffered = if (source is BufferedSource) source else source.buffer()

        return null
    }

    override fun write(sink: Sink, hdu: Hdu) {
        TODO("Not yet implemented")
    }
}
