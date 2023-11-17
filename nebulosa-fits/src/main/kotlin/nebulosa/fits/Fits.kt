package nebulosa.fits

import okio.Sink
import java.io.Closeable
import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path

class Fits private constructor(
    path: Path,
    private val hdus: ArrayList<Hdu<*>>,
) : File("$path"), List<Hdu<*>> by hdus, Closeable {

    constructor(path: Path) : this(path, ArrayList(4))

    constructor(path: String) : this(Path.of(path))

    private val randomAccessFile = RandomAccessFile(this, "r")

    private fun readHdu(): Hdu<*>? {
        return try {
            return FitsIO.read(randomAccessFile.channel).also(::add)
        } catch (ignored: EOFException) {
            null
        }
    }

    fun read() {
        randomAccessFile.channel.position(0L)
        hdus.clear()

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

    fun writeTo(outputStream: Sink) {
        hdus.forEach { FitsIO.write(outputStream, it) }
    }

    override fun close() {
        randomAccessFile.close()
    }
}
