package nebulosa.pixinsight.star.detection

import nebulosa.pixinsight.script.PixInsightDetectStars
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import java.nio.file.Path
import java.time.Duration

data class PixInsightStarDetector(
    private val runner: PixInsightScriptRunner,
    private val slot: Int,
    private val minSNR: Double = 0.0,
    private val timeout: Duration = Duration.ZERO,
) : StarDetector<Path> {

    override fun detect(input: Path): List<ImageStar> {
        return PixInsightDetectStars(slot, input, minSNR, false, timeout)
            .use { it.runSync(runner).stars.toList() }
    }
}
