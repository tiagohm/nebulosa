import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.star.detection.PixInsightStarDetector
import nebulosa.test.AbstractFitsAndXisfTest
import java.nio.file.Path

class PixInsightStarDetectorTest : AbstractFitsAndXisfTest() {

    init {
        "detect stars" {
            val runner = PixInsightScriptRunner(Path.of("PixInsight"))
            val detectedStars = PixInsightStarDetector(runner, 0).detect(NGC3344_MONO_8_FITS)
            detectedStars shouldHaveSize 15
        }
    }
}
