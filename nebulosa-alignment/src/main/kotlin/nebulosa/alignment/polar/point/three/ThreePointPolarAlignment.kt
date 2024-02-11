package nebulosa.alignment.polar.point.three

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.constants.DEG2RAD
import nebulosa.fits.declination
import nebulosa.fits.observationDate
import nebulosa.fits.rightAscension
import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import java.nio.file.Path
import kotlin.math.min

/**
 * Three Point Polar Alignment almost anywhere in the sky.
 *
 * Based on Stefan Berg's algorithm.
 *
 * @see <a href="https://bitbucket.org/Isbeorn/nina.plugin.polaralignment/src/master/">BitBucket</a>
 */
data class ThreePointPolarAlignment(
    private val solver: PlateSolver,
    private val longitude: Angle,
    private val latitude: Angle,
) {

    private val positions = arrayOfNulls<Position>(3)

    @Volatile var state = 0
        private set

    fun align(
        path: Path, image: Image,
        rightAscension: Angle = image.header.rightAscension,
        declination: Angle = image.header.declination,
        radius: Angle = DEFAULT_RADIUS,
        cancellationToken: CancellationToken = CancellationToken.NONE,
    ): ThreePointPolarAlignmentResult {
        val solution = try {
            solver.solve(path, image, rightAscension, declination, radius, cancellationToken = cancellationToken)
        } catch (e: PlateSolvingException) {
            return ThreePointPolarAlignmentResult.NoPlateSolution(e)
        }

        if (!solution.solved) {
            return ThreePointPolarAlignmentResult.NoPlateSolution(null)
        } else {
            val time = image.header.observationDate?.let { UTC(TimeYMDHMS(it)) } ?: UTC.now()

            positions[min(state, 2)] = solution.position(time)

            if (state++ >= 2) {
                val polarErrorDetermination = PolarErrorDetermination(positions[0]!!, positions[1]!!, positions[2]!!, longitude, latitude)
                val (azimuth, altitude) = polarErrorDetermination.compute()
                return ThreePointPolarAlignmentResult.Measured(solution.rightAscension, solution.declination, azimuth, altitude)
            }

            return ThreePointPolarAlignmentResult.NeedMoreMeasurement(solution.rightAscension, solution.declination)
        }
    }

    fun reset() {
        state = 0
        positions.fill(null)
    }

    private fun PlateSolution.position(time: UTC): Position {
        return Position(rightAscension, declination, longitude, latitude, time)
    }

    companion object {

        const val DEFAULT_RADIUS: Angle = 4 * DEG2RAD
    }
}
