package nebulosa.xisf

import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import java.io.Closeable
import java.io.File
import java.nio.file.Path

data class XisfPath(val path: Path) : Xisf(), Closeable {

    private val source = path.seekableSource()
    private val sink = path.seekableSink()

    constructor(file: File) : this(file.toPath())

    constructor(path: String) : this(Path.of(path))

    fun read() {
        read(source)
    }

    fun write() {
        write(sink)
    }

    override fun close() {
        source.close()
        sink.close()
    }
}
