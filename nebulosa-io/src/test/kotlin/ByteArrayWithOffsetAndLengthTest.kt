import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.sink
import nebulosa.io.source
import org.junit.jupiter.api.AfterEach

class ByteArrayWithOffsetAndLengthTest : AbstractSeekableSinkAndSourceTest() {

    private val data = ByteArray(16) { it.toByte() }

    override val sink = data.sink(5, 8)
    override val source = data.source(5, 8)

    @AfterEach
    fun checkValuesAfterEach() {
        for (i in 0..4) {
            data[i].toInt() shouldBeExactly i
        }
        for (i in 13..15) {
            data[i].toInt() shouldBeExactly i
        }
    }
}
