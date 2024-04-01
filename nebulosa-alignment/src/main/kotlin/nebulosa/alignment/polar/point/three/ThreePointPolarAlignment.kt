package nebulosa.alignment.polar.point.three

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.constants.DEG2RAD
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
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
        path: Path,
        rightAscension: Angle, declination: Angle, radius: Angle = DEFAULT_RADIUS,
        compensateRefraction: Boolean = false,
        cancellationToken: CancellationToken = CancellationToken.NONE,
    ): ThreePointPolarAlignmentResult {
        val solution = try {
            solver.solve(path, null, rightAscension, declination, radius, cancellationToken = cancellationToken)
        } catch (e: PlateSolvingException) {
            return ThreePointPolarAlignmentResult.NoPlateSolution(e)
        }

        if (!solution.solved || cancellationToken.isCancelled) {
            return ThreePointPolarAlignmentResult.NoPlateSolution(null)
        } else {
            val time = UTC.now()

            positions[min(state, 2)] = solution.position(time, compensateRefraction)

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

    private fun PlateSolution.position(time: UTC, compensateRefraction: Boolean): Position {
        return Position(rightAscension, declination, longitude, latitude, time, compensateRefraction)
    }

    companion object {

        const val DEFAULT_RADIUS: Angle = 4 * DEG2RAD
    }
}
