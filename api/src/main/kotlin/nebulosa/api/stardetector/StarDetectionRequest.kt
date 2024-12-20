package nebulosa.api.stardetector

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.positiveOrZero
import nebulosa.astap.stardetector.AstapStarDetector
import nebulosa.pixinsight.script.startPixInsight
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
) : Supplier<StarDetector<Path>>, Validatable {

    override fun validate() {
        timeout.positiveOrZero()
        minSNR.positiveOrZero()
        maxStars.positiveOrZero()
        slot.positiveOrZero()
    }

    override fun get() = when (type) {
        StarDetectorType.ASTAP -> AstapStarDetector(executablePath!!, minSNR)
        StarDetectorType.SIRIL -> SirilStarDetector(executablePath!!, maxStars)
        StarDetectorType.PIXINSIGHT -> {
            val runner = startPixInsight(executablePath!!, slot)
            PixInsightStarDetector(runner, minSNR, timeout, slot)
        }
    }

    companion object {

        val EMPTY = StarDetectionRequest()
    }
}
