package nebulosa.pixinsight.star.detection

import nebulosa.pixinsight.script.PixInsightDetectStars
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import java.nio.file.Path

data class PixInsightStarDetector(
    private val runner: PixInsightScriptRunner,
    private val slot: Int,
    private val minSNR: Double = 0.0,
) : StarDetector<Path> {

    override fun detect(input: Path): List<ImageStar> {
        return PixInsightDetectStars(slot, input, minSNR).use { it.runSync(runner).stars.toList() }
    }
}
