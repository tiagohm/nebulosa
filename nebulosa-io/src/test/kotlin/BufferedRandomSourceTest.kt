import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.io.source
import okio.buffer
import java.util.*

class BufferedRandomSourceTest : StringSpec() {

    init {
        "read full segment" {
            val source = Random(0).source(8192L).buffer()
            val bytes = source.readByteArray(8192L)
            bytes.isRandom().shouldBeTrue()
        }
        "read few bytes" {
            val source = Random(0).source(8192L).buffer()
            val bytes = source.readByteArray(256)
            bytes.isRandom().shouldBeTrue()
        }
        "read many bytes" {
            val source = Random(0).source(8192L * 4L).buffer()
            val bytes = source.readByteArray(8192L * 4L)
            bytes.isRandom().shouldBeTrue()
        }
    }

    private fun ByteArray.isRandom(): Boolean {
        val counter = IntArray(256)

        for (byte in this) {
            counter[byte.toInt() and 0xff]++
        }

        val average = counter.average()

        return size / 256 == average.toInt()
    }
}
