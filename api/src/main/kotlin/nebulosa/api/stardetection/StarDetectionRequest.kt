package nebulosa.api.stardetection

import nebulosa.astap.star.detection.AstapStarDetector
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScript
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import nebulosa.pixinsight.star.detection.PixInsightStarDetector
import nebulosa.star.detection.StarDetector
import java.nio.file.Path
import java.time.Duration
import java.util.function.Supplier

data class StarDetectionRequest(
    @JvmField val type: StarDetectorType = StarDetectorType.ASTAP,
    @JvmField val executablePath: Path? = null,
    @JvmField val timeout: Duration = Duration.ZERO,
    @JvmField val minSNR: Double = 0.0,
) : Supplier<StarDetector<Path>> {

    override fun get() = when (type) {
        StarDetectorType.ASTAP -> AstapStarDetector(executablePath!!, minSNR)
        StarDetectorType.PIXINSIGHT -> {
            val runner = PixInsightScriptRunner(executablePath!!)

            if (!PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT).use { it.runSync(runner) }) {
                if (!PixInsightStartup(PixInsightScript.DEFAULT_SLOT).use { it.runSync(runner) }) {
                    throw IllegalStateException("unable to start PixInsight")
                }
            }

            PixInsightStarDetector(runner, PixInsightScript.DEFAULT_SLOT, minSNR, timeout)
        }
    }

    companion object {

        @JvmStatic val EMPTY = StarDetectionRequest()
    }
}
