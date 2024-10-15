import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.astap.stardetector.AstapStarDetector
import nebulosa.test.NonGitHubOnly
import nebulosa.test.fits.ASTROMETRY_GALACTIC_CENTER_FITS
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@NonGitHubOnly
class AstapStarDetectorTest {

    @Test
    fun detect() {
        val solver = AstapStarDetector(Path("astap"))
        val detectedStars = solver.detect(ASTROMETRY_GALACTIC_CENTER_FITS)
        detectedStars shouldHaveSize 429
    }
}
