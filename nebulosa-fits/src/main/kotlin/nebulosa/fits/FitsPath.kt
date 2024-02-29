package nebulosa.fits

import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import java.io.Closeable
import java.io.File
import java.nio.file.Path

class FitsPath(path: Path) : Fits(), Closeable {

    private val source = path.seekableSource()
    private val sink = path.seekableSink()

    constructor(file: File) : this(file.toPath())

    constructor(path: String) : this(Path.of(path))

    fun read() {
        return read(source)
    }

    fun readHdu(): Hdu<*>? {
        return readHdu(source)
    }

    fun writeTo() {
        writeTo(sink)
    }

    override fun close() {
        source.close()
        sink.close()
    }
}