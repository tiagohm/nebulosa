package nebulosa.alignment.polar.point.three

import nebulosa.math.Angle
import nebulosa.platesolver.PlateSolverException

sealed interface ThreePointPolarAlignmentResult {

    data class NeedMoreMeasurement(@JvmField val rightAscension: Angle, @JvmField val declination: Angle) : ThreePointPolarAlignmentResult

    data class Measured(
        @JvmField val rightAscension: Angle, @JvmField val declination: Angle,
        @JvmField val azimuth: Angle, @JvmField val altitude: Angle,
    ) : ThreePointPolarAlignmentResult

    data class NoPlateSolution(@JvmField val exception: PlateSolverException?) : ThreePointPolarAlignmentResult

    data object Cancelled : ThreePointPolarAlignmentResult
}
