import io.kotest.engine.spec.tempfile
import nebulosa.io.seekableSink
import nebulosa.io.seekableSource

class RandomAccessFileTest : AbstractSeekableSinkAndSourceTest() {

    private val file = tempfile()

    override val sink = file.seekableSink()
    override val source = file.seekableSource()

    init {
        file.writeBytes(ByteArray(8))
    }
}
