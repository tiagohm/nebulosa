import io.kotest.core.spec.style.StringSpec
import okio.buffer
import okio.source
import java.io.File

abstract class AbstractImageTest : StringSpec() {

    companion object {

        @JvmStatic
        internal fun File.md5(): String {
            return source().use { it.buffer().readByteString().md5().hex() }
        }
    }
}
