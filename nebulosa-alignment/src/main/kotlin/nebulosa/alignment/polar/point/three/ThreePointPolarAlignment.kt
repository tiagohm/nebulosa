package nebulosa.alignment.polar.point.three

import nebulosa.common.Resettable
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.constants.DEG2RAD
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.plate.solving.PlateSolvingException
import nebulosa.time.UTC
import java.nio.file.Path

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
) : Resettable {

    private val positions = arrayOfNulls<Position>(2)

    @Volatile var state = 0
        private set

    @Volatile var initialAzimuthError: Angle = 0.0
        private set

    @Volatile var initialAltitudeError: Angle = 0.0
        private set

    @Volatile var currentAzimuthError: Angle = 0.0
        private set

    @Volatile var currentAltitudeError: Angle = 0.0
        private set

    private lateinit var polarErrorDetermination: PolarErrorDetermination

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

        if (!solution.solved || cancellationToken.isDone) {
            return ThreePointPolarAlignmentResult.NoPlateSolution(null)
        } else {
            val time = UTC.now()

            if (state > 2) {
                val (azimuth, altitude) = polarErrorDetermination
                    .update(time, initialAzimuthError, initialAltitudeError, solution, compensateRefraction)
                currentAzimuthError = azimuth
                currentAltitudeError = altitude
                return ThreePointPolarAlignmentResult.Measured(solution.rightAscension, solution.declination, azimuth, altitude)
            } else if (state == 2) {
                val position = solution.position(time, compensateRefraction)
                polarErrorDetermination = PolarErrorDetermination(solution, positions[0]!!, positions[1]!!, position, longitude, latitude)
                val (azimuth, altitude) = polarErrorDetermination.compute()
                initialAzimuthError = azimuth
                initialAltitudeError = altitude
                return ThreePointPolarAlignmentResult.Measured(solution.rightAscension, solution.declination, azimuth, altitude)
            } else {
                positions[state] = solution.position(time, compensateRefraction)
                state++
            }

            return ThreePointPolarAlignmentResult.NeedMoreMeasurement(solution.rightAscension, solution.declination)
        }
    }

    override fun reset() {
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
