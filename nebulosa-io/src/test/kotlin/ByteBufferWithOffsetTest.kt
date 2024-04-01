import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.sink
import nebulosa.io.source
import java.nio.ByteBuffer

class ByteBufferWithOffsetTest : AbstractSeekableSinkAndSourceTest() {

    private val bytes = ByteArray(16) { it.toByte() }
    private val data = ByteBuffer.wrap(bytes)

    override val sink = data.sink(8)
    override val source = data.source(8)

    init {
        afterEach {
            for (i in 0..7) {
                bytes[i].toInt() shouldBeExactly i
            }
        }
    }
}
