import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.io.source
import okio.buffer
import org.junit.jupiter.api.Test
import java.util.*

class RandomSourceTest {

    @Test
    fun readFullSegmen() {
        val source = Random(0).source(8192L).buffer()
        val bytes = source.readByteArray(8192L)
        bytes.isRandom().shouldBeTrue()
    }

    @Test
    fun readFewByte() {
        val source = Random(0).source(8192L).buffer()
        val bytes = source.readByteArray(256)
        bytes.isRandom().shouldBeTrue()
    }

    @Test
    fun readManyByte() {
        val source = Random(0).source(8192L * 4L).buffer()
        val bytes = source.readByteArray(8192L * 4L)
        bytes.isRandom().shouldBeTrue()
    }

    companion object {

        @JvmStatic
        private fun ByteArray.isRandom(): Boolean {
            val counter = IntArray(256)

            for (byte in this) {
                counter[byte.toInt() and 0xFF]++
            }

            val average = counter.average()

            return size / 256 == average.toInt()
        }
    }
}
