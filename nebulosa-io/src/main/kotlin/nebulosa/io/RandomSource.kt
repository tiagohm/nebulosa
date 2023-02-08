package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.util.*
import kotlin.math.max
import kotlin.math.min

internal class RandomSource(
    private val random: Random,
    private val maxSize: Long = Long.MAX_VALUE,
    private val timeout: Timeout = Timeout.NONE,
) : SeekableSource {

    private val cursor = Buffer.UnsafeCursor()

    override var position = 0L
        private set

    override val exhausted
        get() = maxSize in 0L..position

    init {
        require(maxSize > 0) { "maxSize <= 0: $maxSize" }
    }

    @Synchronized
    override fun seek(position: Long) {
        val newPos = if (position < 0) maxSize + position else position
        this.position = max(0L, min(newPos, maxSize - 1L))
    }

    @Synchronized
    override fun read(sink: Buffer, byteCount: Long): Long {
        return sink.readAndWriteUnsafe(cursor).use {
            timeout.throwIfReached()

            val size = sink.size
            val length = min(min(maxSize - position, 8192L), byteCount)

            if (length > 0) {
                cursor.expandBuffer(length.toInt())
                random.nextBytes(it.data!!, it.start, length.toInt())
                cursor.resizeBuffer(size + length)
                position += length
                length
            } else {
                cursor.resizeBuffer(size)
                -1L
            }
        }
    }

    override fun timeout() = timeout

    override fun close() = Unit

    companion object {

        private fun Random.nextBytes(
            bytes: ByteArray,
            offset: Int = 0,
            byteCount: Int = bytes.size - offset,
        ) {
            var i = 0

            while (i < byteCount) {
                var rnd = nextInt()
                var n = min(byteCount - i, 4)

                while (n-- > 0) {
                    bytes[offset + i++] = rnd.toByte()
                    rnd = rnd shr 8
                }
            }
        }
    }
}
