import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.nasa.daf.RemoteDaf
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class RemoteDafTest : StringSpec() {

    init {
        val cachePath = Files.createTempDirectory("nebulosa")

        val daf = RemoteDaf("https://ssd.jpl.nasa.gov/ftp/eph/planets/bsp/de441.bsp", cachePath)

        "cache file should be created" {
            val cacheFile = Paths.get("$cachePath", "70fc746e6e9cffbcaaa5af7e5d2b169a-0-1023.cache")
            cacheFile.deleteIfExists()
            daf.read()
            daf.summaries.size shouldBeExactly 28
            cacheFile.exists().shouldBeTrue()
            cacheFile.deleteIfExists()
        }
    }
}
