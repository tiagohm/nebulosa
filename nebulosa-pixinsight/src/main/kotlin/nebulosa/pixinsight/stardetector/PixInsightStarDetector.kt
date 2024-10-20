package nebulosa.pixinsight.stardetector

import nebulosa.pixinsight.script.PixInsightDetectStars
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import java.nio.file.Path
import java.time.Duration

data class PixInsightStarDetector(
    private val runner: PixInsightScriptRunner,
    private val minSNR: Double = 0.0,
    private val timeout: Duration = Duration.ZERO,
    private val slot: Int = PixInsightScript.UNSPECIFIED_SLOT,
) : StarDetector<Path> {

    override fun detect(input: Path): List<StarPoint> {
        return PixInsightDetectStars(slot, input, minSNR, false, timeout)
            .use { it.runSync(runner).stars.toList() }
    }
}
