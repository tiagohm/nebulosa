package nebulosa.io

import okio.Timeout

@Suppress("ArrayInDataClass")
internal data class ByteArraySink(
    private val data: ByteArray,
    private val offset: Int = 0,
    override val size: Long = (data.size - offset).toLong(),
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSink() {

    init {
        require(size > 0) { "size <= 0: $size" }
        checkOffsetAndCount(data.size, offset, size.toInt())
    }

    override fun transfer(input: ByteArray, start: Int, length: Int): Int {
        input.copyInto(data, (offset + position).toInt(), start, start + length)
        return length
    }
}
