package nebulosa.io

import okio.Timeout
import java.nio.ByteBuffer

internal data class ByteBufferSource(
    private val data: ByteBuffer,
    private val offset: Int = 0,
    override val size: Long = (data.remaining() - offset).toLong(),
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSource() {

    private val initialPosition = data.position()

    init {
        require(size > 0) { "size <= 0: $size" }
        checkOffsetAndCount(data.remaining(), offset, size.toInt())
    }

    override fun transfer(output: ByteArray, start: Int, length: Int): Int {
        data.get(initialPosition + offset + position.toInt(), output, start, length)
        return length
    }
}
