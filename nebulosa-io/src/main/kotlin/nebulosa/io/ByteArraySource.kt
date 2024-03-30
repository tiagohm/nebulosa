package nebulosa.io

import okio.Timeout

@Suppress("ArrayInDataClass")
internal data class ByteArraySource(
    private val data: ByteArray,
    private val offset: Int = 0,
    override val size: Long = (data.size - offset).toLong(),
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSource() {

    init {
        require(size > 0) { "size <= 0: $size" }
        checkOffsetAndCount(data.size, offset, size.toInt())
    }

    override fun transfer(output: ByteArray, start: Int, length: Int): Int {
        val startIndex = (offset + position).toInt()
        data.copyInto(output, start, startIndex, startIndex + length)
        return length
    }
}
