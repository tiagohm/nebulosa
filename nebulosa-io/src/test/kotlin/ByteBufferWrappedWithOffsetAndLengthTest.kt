import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.sink
import nebulosa.io.source
import org.junit.jupiter.api.AfterEach
import java.nio.ByteBuffer

class ByteBufferWrappedWithOffsetAndLengthTest : AbstractSeekableSinkAndSourceTest() {

    private val bytes = ByteArray(16) { it.toByte() }
    private val data = ByteBuffer.wrap(bytes, 5, 8)

    override val sink = data.sink()
    override val source = data.source()

    @AfterEach
    fun checkValuesAfterEach() {
        for (i in 0..4) {
            bytes[i].toInt() shouldBeExactly i
        }
        for (i in 13..15) {
            bytes[i].toInt() shouldBeExactly i
        }
    }
}
