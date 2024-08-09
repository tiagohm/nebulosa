import PixInsightScriptTest.Companion.RUNNER
import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.pixinsight.stardetector.PixInsightStarDetector
import nebulosa.test.NGC3344_MONO_8_FITS
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test

@NonGitHubOnly
class PixInsightStarDetectorTest {

    @Test
    fun detectStars() {
        val detectedStars = PixInsightStarDetector(RUNNER, 0).detect(NGC3344_MONO_8_FITS)
        detectedStars shouldHaveSize 15
    }
}
