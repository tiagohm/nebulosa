package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

internal class ByteBufferSource(
    private val data: ByteBuffer,
    private val offset: Int = 0,
    private val byteCount: Int = data.capacity() - offset,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSource {

    private val cursor = Buffer.UnsafeCursor()

    override var position = 0L
        private set

    override val exhausted
        get() = position >= byteCount

    init {
        require(byteCount > 0) { "byteCount <= 0: $byteCount" }
        checkOffsetAndCount(data.capacity(), offset, byteCount)
    }

    @Synchronized
    override fun seek(position: Long) {
        val newPos = if (position < 0) byteCount + position else position
        this.position = max(0L, min(newPos, byteCount.toLong()))
    }

    @Synchronized
    override fun read(sink: Buffer, byteCount: Long): Long {
        if (exhausted) return -1L

        return sink.readAndWriteUnsafe(cursor).use {
            timeout.throwIfReached()

            val size = sink.size
            val length = min(min(this.byteCount - position, 8192L), byteCount)

            if (length > 0L) {
                it.expandBuffer(length.toInt())
                data.get(offset + position.toInt(), it.data!!, it.start, length.toInt())
                it.resizeBuffer(size + length)
                position += length
                length
            } else {
                it.resizeBuffer(size)
                -1L
            }
        }
    }

    override fun timeout() = timeout

    override fun close() = Unit
}
