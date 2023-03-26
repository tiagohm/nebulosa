package nebulosa.guiding.internal

import nebulosa.math.Angle

data class Calibration(
    val xRate: Double = 0.0,
    val yRate: Double = 0.0,
    val xAngle: Angle = Angle.ZERO,
    val yAngle: Angle = Angle.ZERO,
    val declination: Angle = Angle.ZERO,
    val rotatorAngle: Angle = Angle.ZERO,
    val binning: Int = 1,
    val pierSideAtEast: Boolean = false,
    val raGuideParity: GuideParity = GuideParity.UNKNOWN,
    val decGuideParity: GuideParity = GuideParity.UNKNOWN,
) {

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
