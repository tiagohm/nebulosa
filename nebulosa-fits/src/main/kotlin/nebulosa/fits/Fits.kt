package nebulosa.fits

import nebulosa.io.SeekableSource
import nebulosa.io.seekableSource
import okio.Sink
import java.io.Closeable
import java.io.EOFException
import java.io.File
import java.nio.file.Path

class Fits private constructor(
    val source: SeekableSource?,
    private val hdus: ArrayList<Hdu<*>>,
) : List<Hdu<*>> by hdus, Closeable {

    constructor(source: SeekableSource? = null) : this(source, ArrayList(4))

    constructor(path: File) : this(path.seekableSource())

    constructor(path: Path) : this(path.toFile())

    constructor(path: String) : this(File(path))

    fun readHdu(): Hdu<*>? {
        if (source == null) return null

        return try {
            return FitsIO.read(source).also(::add)
        } catch (ignored: EOFException) {
            null
        }
    }

    fun read() {
        while (true) {
            readHdu() ?: break
        }
    }

    fun add(hdu: Hdu<*>) {
        hdus.add(hdu)
    }

    fun remove(hdu: Hdu<*>): Boolean {
        return hdus.remove(hdu)
    }

    fun clear() {
        hdus.clear()
    }

    fun writeTo(sink: Sink) {
        hdus.forEach { FitsIO.write(sink, it) }
    }

    override fun close() {
        source?.close()
    }
}
