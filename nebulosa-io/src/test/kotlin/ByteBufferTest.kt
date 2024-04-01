import nebulosa.io.sink
import nebulosa.io.source
import java.nio.ByteBuffer

class ByteBufferTest : AbstractSeekableSinkAndSourceTest() {

    private val data = ByteBuffer.allocate(8)

    override val sink = data.sink()
    override val source = data.source()
}
