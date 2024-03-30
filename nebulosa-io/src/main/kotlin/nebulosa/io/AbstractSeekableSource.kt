package nebulosa.io

import okio.Buffer
import okio.Timeout
import kotlin.math.max
import kotlin.math.min

abstract class AbstractSeekableSource : SeekableSource {

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

    protected abstract fun transfer(output: ByteArray, start: Int, length: Int): Int

    protected open fun computeTransferedSize(unsafeCursor: Buffer.UnsafeCursor, byteCount: Long): Long {
        return min(min(size - position, 8192L), byteCount)
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (exhausted) return -1L

        return sink.readAndWriteUnsafe(cursor).use {
            timeout.throwIfReached()

            val length = computeTransferedSize(it, byteCount)
            val sinkSize = sink.size

            if (length > 0L) {
                cursor.expandBuffer(length.toInt())

                val currentPosition = position
                val transferedSize = transfer(it.data!!, it.start, length.toInt()).toLong()
                cursor.resizeBuffer(sinkSize + transferedSize)
                position = currentPosition + transferedSize
                transferedSize
            } else {
                cursor.resizeBuffer(sinkSize)
                -1L
            }
        }
    }

    override fun timeout() = timeout

    override fun close() = cursor.close()
}
