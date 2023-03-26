package nebulosa.guiding.internal

import nebulosa.math.Angle

data class Calibration(
    var xRate: Double = 0.0,
    var yRate: Double = 0.0,
    var xAngle: Angle = Angle.ZERO,
    var yAngle: Angle = Angle.ZERO,
    var declination: Angle = Angle.ZERO,
    var rotatorAngle: Angle = Angle.ZERO,
    var binning: Int = 1,
    var pierSideAtEast: Boolean = false,
    var raGuideParity: GuideParity = GuideParity.UNKNOWN,
    var decGuideParity: GuideParity = GuideParity.UNKNOWN,
) {

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
