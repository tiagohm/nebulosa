import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import kotlin.io.path.writeBytes

class RandomAccessFileTest : AbstractSeekableSinkAndSourceTest() {

    private val file = tempPath("raf-", ".dat")

    override val sink = file.seekableSink().autoClose()
    override val source = file.seekableSource().autoClose()

    init {
        file.writeBytes(ByteArray(8))
    }
}
