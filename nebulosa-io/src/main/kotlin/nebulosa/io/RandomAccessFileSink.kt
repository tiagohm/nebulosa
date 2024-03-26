package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

internal class RandomAccessFileSink(
    private val file: RandomAccessFile,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSink {

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
    override fun write(source: Buffer, byteCount: Long) {
        if (!file.channel.isOpen) throw IllegalStateException("closed")
        if (exhausted) throw IllegalStateException("exhausted")

        if (byteCount == 0L) return

        var remaining = byteCount

        while (remaining > 0) {
            timeout.throwIfReached()

            source.readUnsafe(cursor).use {
                it.seek(0L)
                val length = min(it.remaining.toLong(), remaining)
                file.write(it.data!!, it.start, length.toInt())
                remaining -= length
                source.skip(length)
            }
        }
    }

    override fun timeout() = timeout

    override fun flush() = Unit

    override fun close() = Unit
}
