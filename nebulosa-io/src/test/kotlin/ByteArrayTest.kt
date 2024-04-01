import nebulosa.io.sink
import nebulosa.io.source

class ByteArrayTest : AbstractSeekableSinkAndSourceTest() {

    private val data = ByteArray(8)

    override val sink = data.sink()
    override val source = data.source()
}
