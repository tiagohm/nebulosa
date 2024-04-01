package nebulosa.xisf

import nebulosa.image.format.Hdu
import nebulosa.image.format.ImageRepresentation
import nebulosa.io.SeekableSource
import okio.Sink
import java.util.*

/**
 * Represents a XISF image.
 */
open class Xisf : LinkedList<Hdu<*>>, ImageRepresentation {

    constructor() : super()

    constructor(hdus: Collection<Hdu<*>>) : super(hdus)

    final override fun read(source: SeekableSource) {
        addAll(XisfFormat.read(source))
    }

    final override fun write(sink: Sink) {
        XisfFormat.write(sink, this)
    }
}
