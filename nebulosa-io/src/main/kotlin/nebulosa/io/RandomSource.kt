package nebulosa.io

import okio.Timeout
import java.util.*
import kotlin.math.min

internal data class RandomSource(
    private val random: Random,
    override val size: Long = Long.MAX_VALUE,
    override val timeout: Timeout = Timeout.NONE,
) : AbstractSeekableSource() {

    init {
        require(size > 0) { "size <= 0: $size" }
    }

    override fun transfer(output: ByteArray, start: Int, length: Int): Int {
        random.nextBytes(output, start, length)
        return length
    }

    companion object {

        @JvmStatic
        private fun Random.nextBytes(bytes: ByteArray, offset: Int = 0, byteCount: Int = bytes.size - offset) {
            var i = 0

            while (i < byteCount) {
                var rnd = nextInt()

                repeat(min(byteCount - i, 4)) {
                    bytes[offset + i++] = rnd.toByte()
                    rnd = rnd shr 8
                }
            }
        }
    }
}
