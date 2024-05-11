package nebulosa.alignment.polar.point.three

import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import kotlin.math.abs

internal data class PolarErrorDetermination(
    @JvmField val firstPosition: Position,
    @JvmField val secondPosition: Position,
    @JvmField val thirdPosition: Position,
    @JvmField val longitude: Angle,
    @JvmField val latitude: Angle,
) {

    private inline val isNorthern
        get() = latitude > 0.0

    @JvmField val plane = with(Vector3D.plane(firstPosition.vector, secondPosition.vector, thirdPosition.vector)) {
        // Flip vector if pointing to the wrong direction.
        if (isNorthern && x < 0 || !isNorthern && x > 0) -normalized else normalized
    }

    @JvmField val errorPosition = Position(plane)

    fun compute(): DoubleArray {
        val altitudeError: Double
        var azimuthError: Double

        val pole = abs(latitude)

        if (isNorthern) {
            altitudeError = errorPosition.topocentric.altitude - pole
            azimuthError = errorPosition.topocentric.azimuth
        } else {
            altitudeError = pole - errorPosition.topocentric.altitude
            azimuthError = errorPosition.topocentric.azimuth + PI
        }

        if (azimuthError > PI) {
            azimuthError -= TAU
        }
        if (azimuthError < -PI) {
            azimuthError += TAU
        }

        return doubleArrayOf(azimuthError, altitudeError)
    }
}
