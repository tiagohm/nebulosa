package nebulosa.api.stardetector

import nebulosa.astap.stardetector.AstapStarDetector
import nebulosa.pixinsight.script.PixInsightIsRunning
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.script.PixInsightStartup
import nebulosa.pixinsight.stardetector.PixInsightStarDetector
import nebulosa.siril.stardetector.SirilStarDetector
import nebulosa.stardetector.StarDetector
import java.nio.file.Path
import java.time.Duration
import java.util.function.Supplier

data class StarDetectionRequest(
    @JvmField val type: StarDetectorType = StarDetectorType.ASTAP,
    @JvmField val executablePath: Path? = null,
    @JvmField val timeout: Duration = Duration.ZERO,
    @JvmField val minSNR: Double = 0.0,
    @JvmField val maxStars: Int = 0,
    @JvmField val slot: Int = 1,
) : Supplier<StarDetector<Path>> {

    override fun get() = when (type) {
        StarDetectorType.ASTAP -> AstapStarDetector(executablePath!!, minSNR)
        StarDetectorType.SIRIL -> SirilStarDetector(executablePath!!, maxStars)
        StarDetectorType.PIXINSIGHT -> {
            val runner = PixInsightScriptRunner(executablePath!!)

            if (!PixInsightIsRunning(slot).use { it.runSync(runner) }) {
                if (!PixInsightStartup(slot).use { it.runSync(runner) }) {
                    throw IllegalStateException("unable to start PixInsight")
                }
            }

            PixInsightStarDetector(runner, slot, minSNR, timeout)
        }
    }

    companion object {

        @JvmStatic val EMPTY = StarDetectionRequest()
    }
}
