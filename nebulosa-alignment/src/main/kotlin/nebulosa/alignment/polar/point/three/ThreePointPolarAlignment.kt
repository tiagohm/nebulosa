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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

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
                    val (azimuth, altitude) = polarErrorDetermination.update(time, initialAzimuthError, initialAltitudeError, solution)
                    currentAzimuthError = azimuth
                    currentAltitudeError = altitude
                    return Measured(solution.rightAscension, solution.declination, azimuth, altitude)
                }
                State.THIRD_POSITION -> {
                    val position = solution.position(time)
                    polarErrorDetermination = PolarErrorDetermination(solution, firstPosition, secondPosition, position, longitude, latitude)
                    val (azimuth, altitude) = polarErrorDetermination.compute()
                    state = State.CONTINUOUS_SOLVE
                    initialAzimuthError = azimuth
                    initialAltitudeError = altitude
                    return Measured(solution.rightAscension, solution.declination, azimuth, altitude)
                }
                State.SECOND_POSITION -> {
                    secondPosition = solution.position(time)
                    state = State.THIRD_POSITION
                }
                State.FIRST_POSITION -> {
                    firstPosition = solution.position(time)
                    state = State.SECOND_POSITION
                }
            }

            return NeedMoreMeasurement(solution.rightAscension, solution.declination)
        }
    }

    override fun reset() {
        state = State.FIRST_POSITION
    }

    private fun PlateSolution.position(time: UTC): Position {
        return Position(rightAscension, declination, longitude, latitude, time)
    }

    companion object {

        const val DEFAULT_RADIUS: Angle = 5 * DEG2RAD

        /**
         * Returns the RA/DEC polar alignment error in radians, given
         * the [hourAngle] and [declination] of current target,
         * the [latitude] of current observation site and
         * the [azimuthError], [altitudeError] alignment errors.
         */
        fun computePAE(
            hourAngle: Angle, declination: Angle, latitude: Angle,
            azimuthError: Angle, altitudeError: Angle,
        ): DoubleArray {
            // Source: https://sourceforge.net/p/sky-simulator/code/ci/default/tree/sky_annotation.pas
            // Polar error calculation based on two celestial reference points and the error of the telescope mount at these point(s).
            // Based on formulas from Ralph Pass documented at https://rppass.com/align.pdf.
            // They are based on the book “Telescope Control’ by Trueblood and Genet, p.111
            // Ralph added sin(latitude) term in the equation for the error in RA.

            // For one reference image the difference in RA and DEC caused by the misalignment of the polar axis, formula (3):
            //    delta_ra:= de * TAN(dec)*SIN(h)  + da * (sin(lat)- COS(lat)*(TAN(dec1)*COS(h_1))
            //    delta_dec:=de * COS(h)  + da * COS(lat)*SIN(h))}
            //    raJnow0:=raJnow0-dRa;
            //    decJnow0:=decJnow0+dDec;

            val cosHA = cos(hourAngle)
            val sinHA = sin(hourAngle)
            val tanDEC = tan(declination)
            val cosLat = cos(latitude)
            val sinLat = sin(latitude)
            val dRA = altitudeError * tanDEC * sinHA + azimuthError * (sinLat - cosLat * tanDEC * cosHA)
            val dDEC = -altitudeError * cosHA + azimuthError * cosLat * sinHA
            return doubleArrayOf(dRA, dDEC)
        }
    }
}
