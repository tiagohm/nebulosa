package nebulosa.api.stardetection

import nebulosa.astap.star.detection.AstapStarDetector
import nebulosa.star.detection.StarDetector
import java.nio.file.Path
import java.time.Duration
import java.util.function.Supplier

data class StarDetectionOptions(
    @JvmField val type: StarDetectorType = StarDetectorType.ASTAP,
    @JvmField val executablePath: Path? = null,
    @JvmField val timeout: Duration = Duration.ZERO,
) : Supplier<StarDetector<Path>> {

    override fun get() = when (type) {
        StarDetectorType.ASTAP -> AstapStarDetector(executablePath!!)
    }

    companion object {

        @JvmStatic val EMPTY = StarDetectionOptions()
    }
}
