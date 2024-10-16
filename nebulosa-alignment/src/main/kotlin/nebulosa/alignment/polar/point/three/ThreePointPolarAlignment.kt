package nebulosa.alignment.polar.point.three

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignmentResult.*
import nebulosa.constants.DEG2RAD
import nebulosa.math.Angle
import nebulosa.platesolver.PlateSolution
import nebulosa.platesolver.PlateSolver
import nebulosa.platesolver.PlateSolverException
import nebulosa.time.UTC
import nebulosa.util.Resettable
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

    enum class State {
        FIRST_POSITION,
        SECOND_POSITION,
        THIRD_POSITION,
        CONTINUOUS_SOLVE,
    }

    @Volatile var state = State.FIRST_POSITION
        private set

    @Volatile var initialAzimuthError: Angle = 0.0
        private set

    @Volatile var initialAltitudeError: Angle = 0.0
        private set

    @Volatile var currentAzimuthError: Angle = 0.0
        private set

    @Volatile var currentAltitudeError: Angle = 0.0
        private set

    private lateinit var firstPosition: Position
    private lateinit var secondPosition: Position
    private lateinit var polarErrorDetermination: PolarErrorDetermination

    fun align(
        path: Path,
        rightAscension: Angle, declination: Angle, radius: Angle = DEFAULT_RADIUS,
        compensateRefraction: Boolean = false,
    ): ThreePointPolarAlignmentResult {
        val solution = try {
            solver.solve(path, null, rightAscension, declination, radius)
        } catch (e: PlateSolverException) {
            return NoPlateSolution(e)
        } catch (e: Throwable) {
            return NoPlateSolution(null)
        }

        if (!solution.solved) {
            return NoPlateSolution(null)
        } else {
            val time = UTC.now()

            when (state) {
                State.CONTINUOUS_SOLVE -> {
                    val (azimuth, altitude) = polarErrorDetermination
                        .update(time, initialAzimuthError, initialAltitudeError, solution, compensateRefraction)
                    currentAzimuthError = azimuth
                    currentAltitudeError = altitude
                    return Measured(solution.rightAscension, solution.declination, azimuth, altitude)
                }
                State.THIRD_POSITION -> {
                    val position = solution.position(time, compensateRefraction)
                    polarErrorDetermination = PolarErrorDetermination(solution, firstPosition, secondPosition, position, longitude, latitude)
                    val (azimuth, altitude) = polarErrorDetermination.compute()
                    state = State.CONTINUOUS_SOLVE
                    initialAzimuthError = azimuth
                    initialAltitudeError = altitude
                    return Measured(solution.rightAscension, solution.declination, azimuth, altitude)
                }
                State.SECOND_POSITION -> {
                    secondPosition = solution.position(time, compensateRefraction)
                    state = State.THIRD_POSITION
                }
                State.FIRST_POSITION -> {
                    firstPosition = solution.position(time, compensateRefraction)
                    state = State.SECOND_POSITION
                }
            }

            return NeedMoreMeasurement(solution.rightAscension, solution.declination)
        }
    }

    override fun reset() {
        state = State.FIRST_POSITION
    }

    private fun PlateSolution.position(time: UTC, compensateRefraction: Boolean): Position {
        return Position(rightAscension, declination, longitude, latitude, time, compensateRefraction)
    }

    companion object {

        const val DEFAULT_RADIUS: Angle = 5 * DEG2RAD
    }
}
