package nebulosa.fits

import nebulosa.io.SeekableSource
import okio.Sink
import java.io.EOFException
import java.util.*

open class Fits : LinkedList<Hdu<*>> {

    constructor() : super()

    constructor(hdus: Collection<Hdu<*>>) : super(hdus)

    fun readHdu(source: SeekableSource): Hdu<*>? {
        return try {
            return FitsIO.read(source).also(::add)
        } catch (ignored: EOFException) {
            null
        }
    }

    fun read(source: SeekableSource) {
        while (true) {
            readHdu(source) ?: break
        }
    }

    fun writeTo(sink: Sink) {
        forEach { FitsIO.write(sink, it) }
    }
}
