import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.test.AbstractTest
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class RemoteDafTest : AbstractTest() {

    @Test
    fun cacheFileShouldBeCreated() {
        val cachePath = tempDirectory("daf-")
        val daf = RemoteDaf("https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp", cachePath)

        val cacheFile = Paths.get("$cachePath", "70fc746e6e9cffbcaaa5af7e5d2b169a-0-1023.cache")
        cacheFile.deleteIfExists()
        daf.read()
        daf.summaries.size shouldBeExactly 28
        cacheFile.exists().shouldBeTrue()
        cacheFile.deleteIfExists()
    }
}
