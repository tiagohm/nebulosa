package nebulosa.alignment.polar.point.three

import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.plate.solving.PlateSolution

internal data class PolarErrorDetermination(
    @JvmField val initialReferenceFrame: PlateSolution,
    @JvmField val firstPosition: Vector3D,
    @JvmField val secondPosition: Vector3D,
    @JvmField val thirdPosition: Vector3D,
    @JvmField val longitude: Angle, @JvmField val latitude: Angle,
) {

    inline val isNorthern
        get() = latitude > 0.0

    @JvmField val plane = with(Vector3D.plane(firstPosition, secondPosition, thirdPosition)) {
        if (isNorthern && x < 0 || !isNorthern && x > 0) {
            // Flip vector if pointing to the wrong direction.
            -this
        } else {
            this
        }
    }

    @JvmField val initialMountAxisErrorPosition = Position(plane, longitude, latitude)

//    init {
//        calculateMountAxisError()
//    }
//
//    val isInitialErrorLarge
//        get() = initialMountAxisTotalError.Degree > 2 && initialMountAxisTotalError.Degree <= 10
//
//    val isInitialErrorHuge
//        get() = initialMountAxisTotalError.Degree > 10
//
//    private fun calculateMountAxisError() {
//        val altitudeError: Double
//        var azimuthError: Double
//
//        val pole = abs(latitude)
//
//        if (isNorthern) {
//            altitudeError = initialMountAxisErrorPosition.topocentric.altitude - pole
//            azimuthError = initialMountAxisErrorPosition.topocentric.azimuth
//        } else {
//            altitudeError = pole - initialMountAxisErrorPosition.topocentric.altitude
//            azimuthError = initialMountAxisErrorPosition.topocentric.azimuth + PI
//        }
//
//        if (azimuthError > PI) {
//            azimuthError -= TAU
//        }
//        if (azimuthError < -PI) {
//            azimuthError += TAU
//        }
//
//        initialMountAxisAltitudeError = altitudeError
//        initialMountAxisAzimuthError = azimuthError
//        initialMountAxisTotalError = hypot(initialMountAxisAltitudeError, initialMountAxisAzimuthError)
//
//        currentMountAxisAltitudeError = altitudeError
//        currentMountAxisAzimuthError = azimuthError
//        currentMountAxisTotalError = hypot(initialMountAxisAltitudeError, initialMountAxisAzimuthError)
//        currentReferenceFrame = initialReferenceFrame
//    }
}
