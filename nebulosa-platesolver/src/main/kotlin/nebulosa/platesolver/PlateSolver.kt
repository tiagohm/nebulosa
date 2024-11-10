package nebulosa.platesolver

import nebulosa.image.Image
import nebulosa.math.Angle
import java.nio.file.Path
import java.time.Duration

interface PlateSolver {

    fun solve(
        path: Path?, image: Image?,
        centerRA: Angle = 0.0, centerDEC: Angle = 0.0, radius: Angle = 0.0,
        downsampleFactor: Int = 0, timeout: Duration = Duration.ZERO,
    ): PlateSolution

    companion object {

        /**
         * Computes the FOV in arcsec/pixel from [focalLength] in mm and [pixelSize] in µm.
         */
        fun computeFOV(focalLength: Double, pixelSize: Double): Double {
            return if (focalLength <= 0.0) 0.0 else (pixelSize / focalLength) * 206.265
        }
    }
}
