package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.EOFException
import kotlin.math.max
import kotlin.math.min

internal class ByteArraySink(
    private val data: ByteArray,
    private val offset: Int = 0,
    private val byteCount: Int = data.size - offset,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSink {

    private val cursor = Buffer.UnsafeCursor()

    override var position = 0L
        private set

    override val exhausted
        get() = position >= byteCount

    init {
        require(byteCount > 0) { "byteCount <= 0: $byteCount" }
        checkOffsetAndCount(data.size, offset, byteCount)
    }

    @Synchronized
    override fun seek(position: Long) {
        val newPos = if (position < 0) byteCount + position else position
        this.position = max(0L, min(newPos, byteCount.toLong()))
    }

    @Synchronized
    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount == 0L) return

        if (exhausted) throw EOFException("exhausted")

        var remaining = byteCount

        while (remaining > 0) {
            timeout.throwIfReached()

            source.readUnsafe(cursor).use {
                it.seek(0L)

                val length = min(min(this.byteCount - position, it.remaining.toLong()), remaining)

                if (length > 0) {
                    it.data!!.copyInto(data, (offset + position).toInt(), it.start, length.toInt())
                    remaining -= length
                    position += length
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
