package nebulosa.fits

import nebulosa.image.format.Hdu
import nebulosa.image.format.ImageRepresentation
import nebulosa.io.SeekableSource
import okio.Sink
import java.util.*

open class Fits : LinkedList<Hdu<*>>, ImageRepresentation {

    constructor() : super()

    constructor(hdus: Collection<Hdu<*>>) : super(hdus)

    override fun read(source: SeekableSource) {
        addAll(FitsFormat.read(source))
    }

    override fun write(sink: Sink) {
        FitsFormat.write(sink, this)
    }
}
