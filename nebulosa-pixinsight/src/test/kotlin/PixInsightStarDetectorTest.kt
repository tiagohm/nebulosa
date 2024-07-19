import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.stardetector.PixInsightStarDetector
import nebulosa.test.NGC3344_MONO_8_FITS
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test
import java.nio.file.Path

@NonGitHubOnly
class PixInsightStarDetectorTest {

    @Test
    fun detectStars() {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val detectedStars = PixInsightStarDetector(runner, 0).detect(NGC3344_MONO_8_FITS)
        detectedStars shouldHaveSize 15
    }
}
