package nebulosa.fits

import nebulosa.io.SeekableSource
import okio.Sink
import java.io.EOFException
import java.util.*

open class Fits : LinkedList<Hdu<*>> {

    constructor() : super()

    constructor(hdus: Collection<Hdu<*>>) : super(hdus)

    open fun readHdu(source: SeekableSource): Hdu<*>? {
        return try {
            return FitsIO.read(source).also(::add)
        } catch (ignored: EOFException) {
            null
        }
    }

    open fun read(source: SeekableSource) {
        while (true) {
            readHdu(source) ?: break
        }
    }

    open fun writeTo(sink: Sink) {
        writeTo(sink, FitsIO)
    }

    open fun writeTo(sink: Sink, writer: FitsWriter) {
        forEach { writer.write(sink, it) }
    }
}
