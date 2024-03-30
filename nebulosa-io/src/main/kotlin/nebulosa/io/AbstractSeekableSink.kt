package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.EOFException
import kotlin.math.max
import kotlin.math.min

abstract class AbstractSeekableSink : SeekableSink {

    private val cursor = Buffer.UnsafeCursor()

    abstract val size: Long
    abstract val timeout: Timeout

    override var position = 0L
        protected set

    override val exhausted
        get() = position >= size

    override fun seek(position: Long) {
        val newPos = if (position < 0) size + position else position
        this.position = max(0L, min(newPos, size))
    }

    protected abstract fun transfer(input: ByteArray, start: Int, length: Int): Int

    protected open fun computeTransferedSize(unsafeCursor: Buffer.UnsafeCursor, byteCount: Long): Long {
        return min(min(size - position, unsafeCursor.remaining.toLong()), byteCount)
    }

    override fun write(source: Buffer, byteCount: Long) {
        if (byteCount == 0L) return

        // if (exhausted) throw EOFException("exhausted")

        var remaining = byteCount

        while (remaining > 0L) {
            timeout.throwIfReached()

            source.readUnsafe(cursor).use {
                it.seek(0L)

                val length = computeTransferedSize(it, remaining)

                if (length > 0L) {
                    val currentPosition = position
                    val transferedSize = transfer(it.data!!, it.start, length.toInt()).toLong()
                    position = currentPosition + transferedSize
                    remaining -= transferedSize
                    source.skip(transferedSize)
                } else {
                    throw EOFException("exhausted")
                }
            }
        }
    }

    override fun timeout() = timeout

    override fun flush() = Unit

    override fun close() = cursor.close()
}
