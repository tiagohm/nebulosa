package nebulosa.io

import okio.Buffer
import okio.Timeout
import java.io.RandomAccessFile
import kotlin.math.min

internal data class RandomAccessFileSource(
    private val file: RandomAccessFile,
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSource() {

    override val size
        get() = file.length()

    override var position
        get() = file.filePointer
        set(value) {
            file.seek(value)
        }

    override fun computeTransferedSize(unsafeCursor: Buffer.UnsafeCursor, byteCount: Long): Long {
        return min(8192L, byteCount)
    }

    override fun transfer(output: ByteArray, start: Int, length: Int): Int {
        return file.read(output, start, length)
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        check(file.channel.isOpen) { "closed" }
        return super.read(sink, byteCount)
    }

    override fun close() {
        super.close()
        file.close()
    }
}
