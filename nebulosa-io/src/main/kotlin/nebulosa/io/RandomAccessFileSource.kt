package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.Closeable
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

internal class RandomAccessFileSource(
    private val file: RandomAccessFile,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSource, Closeable by file {

    private val cursor = Buffer.UnsafeCursor()

    override val position
        get() = file.filePointer

    override val exhausted
        get() = position >= file.length()

    @Synchronized
    override fun seek(position: Long) {
        val size = file.length()
        if (size <= 0) return
        val newPos = if (position < 0) size + position else position
        file.seek(max(0L, min(newPos, size)))
    }

    @Synchronized
    override fun read(sink: Buffer, byteCount: Long): Long {
        if (!file.channel.isOpen) throw IllegalStateException("closed")
        if (exhausted) throw IllegalStateException("exhausted")

        return sink.readAndWriteUnsafe(cursor).use {
            timeout.throwIfReached()

            val size = sink.size
            val length = min(8192L, byteCount)

            it.expandBuffer(length.toInt())
            val readCount = file.read(it.data!!, it.start, length.toInt())

            if (readCount == -1) {
                it.resizeBuffer(size)
                -1L
            } else {
                cursor.resizeBuffer(size + readCount)
                return readCount.toLong()
            }
        }
    }

    override fun timeout() = timeout
}
