package nebulosa.io

import okio.Buffer
import okio.Timeout
import kotlin.math.max
import kotlin.math.min

internal class ByteArraySource(
    private val data: ByteArray,
    private val offset: Int = 0,
    private val byteCount: Int = data.size - offset,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSource {

    private val cursor = Buffer.UnsafeCursor()

    override var position = 0L
        private set

    override val exhausted get() = position >= byteCount

    init {
        require(byteCount > 0) { "byteCount <= 0: $byteCount" }
        checkOffsetAndCount(data.size, offset, byteCount)
    }

    @Synchronized
    override fun seek(position: Long) {
        val newPos = if (position < 0) byteCount + position else position
        this.position = max(0L, min(newPos, byteCount - 1L))
    }

    @Synchronized
    override fun read(sink: Buffer, byteCount: Long): Long {
        return sink.readAndWriteUnsafe(cursor).use {
            timeout.throwIfReached()

            val size = sink.size
            val length = min(min(this.byteCount - position, 8192L), byteCount)

            if (length > 0) {
                it.expandBuffer(length.toInt())
                val startIndex = offset + position
                data.copyInto(it.data!!, it.start, startIndex.toInt(), (startIndex + length).toInt())
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
