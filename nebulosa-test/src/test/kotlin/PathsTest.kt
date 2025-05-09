import io.kotest.matchers.string.shouldEndWith
import nebulosa.test.LinuxOnly
import nebulosa.test.cacheDirectory
import nebulosa.test.dataDirectory
import nebulosa.test.projectDirectory
import nebulosa.test.rootDirectory
import org.junit.jupiter.api.Test

@LinuxOnly
class PathsTest {

    @Test
    fun rootDirectory() {
        "$rootDirectory" shouldEndWith "/nebulosa"
    }

    @Test
    fun projectDirectory() {
        "$projectDirectory" shouldEndWith "/nebulosa-test"
    }

    @Test
    fun cacheDirectory() {
        "$cacheDirectory" shouldEndWith "/.cache"
    }

    @Test
    fun dataDirectory() {
        "$dataDirectory" shouldEndWith "/data"
    }
}
