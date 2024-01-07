package nebulosa.fits

import nebulosa.io.SeekableSource
import okio.Sink

interface FitsElement {

    fun read(source: SeekableSource)

    fun write(sink: Sink)
}
