package nebulosa.alignment.polar.point.three

import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolvingException

sealed interface ThreePointPolarAlignmentResult {

    data object NeedMoreMeasurement : ThreePointPolarAlignmentResult

    data class Measured(@JvmField val azimuth: Angle, @JvmField val altitude: Angle) : ThreePointPolarAlignmentResult

    data class NoPlateSolution(@JvmField val exception: PlateSolvingException?) : ThreePointPolarAlignmentResult
}
