package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.EOFException
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

internal class ByteBufferSink(
    private val data: ByteBuffer,
    private val offset: Int = 0,
    private val byteCount: Int = data.capacity() - offset,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSink {

    private val cursor = Buffer.UnsafeCursor()

    override var position
        get() = data.position().toLong()
        private set(value) {
            data.position(value.toInt())
        }

    override val exhausted get() = position >= byteCount

    init {
        require(byteCount > 0) { "byteCount <= 0: $byteCount" }
        checkOffsetAndCount(data.capacity(), offset, byteCount)
    }

    @Synchronized
    override fun seek(position: Long) {
        val newPos = if (position < 0) byteCount + position else position
        this.position = max(0L, min(newPos, byteCount - 1L))
    }

    @Synchronized
    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount == 0L) return

        var remaining = byteCount

        while (remaining > 0) {
            timeout.throwIfReached()

            source.readUnsafe(cursor).use {
                it.seek(0L)

                val length = min(min(this.byteCount - position, it.remaining.toLong()), remaining)

                if (length > 0) {
                    data.put(it.data!!, it.start, length.toInt())
                    remaining -= length
                    source.skip(length)
                } else {
                    throw EOFException("exhausted")
                }
            }
        }
    }

    override fun timeout() = timeout

    override fun flush() = Unit

    override fun close() = Unit
}
