import io.kotest.core.spec.style.StringSpec
import okio.buffer
import okio.source
import java.io.File

abstract class ImageTest : StringSpec() {

    protected fun File.md5(): String {
        return source().use { it.buffer().readByteString().md5().hex() }
    }
}
