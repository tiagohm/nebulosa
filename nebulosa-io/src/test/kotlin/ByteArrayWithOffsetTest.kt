import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.io.sink
import nebulosa.io.source
import org.junit.jupiter.api.AfterEach

class ByteArrayWithOffsetTest : AbstractSeekableSinkAndSourceTest() {

    private val data = ByteArray(16) { it.toByte() }

    override val sink = data.sink(8)
    override val source = data.source(8)

    @AfterEach
    fun checkValuesAfterEach() {
        for (i in 0..7) {
            data[i].toInt() shouldBeExactly i
        }
    }
}
