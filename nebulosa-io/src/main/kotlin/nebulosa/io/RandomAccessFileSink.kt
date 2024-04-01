package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

internal data class RandomAccessFileSink(
    private val file: RandomAccessFile,
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSink() {

    override val size
        get() = file.length()

    override var position
        get() = file.filePointer
        set(value) {
            file.seek(max(0L, min(value, size)))
        }

    override fun computeTransferedSize(unsafeCursor: Buffer.UnsafeCursor, byteCount: Long): Long {
        return min(unsafeCursor.remaining.toLong(), byteCount)
    }

    override fun transfer(input: ByteArray, start: Int, length: Int): Int {
        file.write(input, start, length)
        return length
    }

    override fun write(source: Buffer, byteCount: Long) {
        check(file.channel.isOpen) { "closed" }
        super.write(source, byteCount)
    }
}
