package nebulosa.xisf

import nebulosa.io.sink
import nebulosa.io.source
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path

data class XisfPath(val path: Path) : Xisf(), AutoCloseable {

    private val file = RandomAccessFile(path.toFile(), "rw")
    private val source = file.source()
    private val sink = file.sink()

    constructor(file: File) : this(file.toPath())

    fun read() {
        read(source)
    }

    fun write() {
        write(sink)
    }

    override fun close() {
        source.close()
        sink.close()
        file.close()
    }
}
