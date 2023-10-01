package nebulosa.guiding

import nebulosa.math.Angle

data class GuideCalibration(
    val xRate: Double = 0.0,
    val yRate: Double = 0.0,
    val xAngle: Angle = 0.0,
    val yAngle: Angle = 0.0,
    val declination: Angle = 0.0,
    val rotatorAngle: Angle = 0.0,
    val binning: Int = 1,
    val pierSideAtEast: Boolean = false,
    val raGuideParity: GuideParity = GuideParity.UNKNOWN,
    val decGuideParity: GuideParity = GuideParity.UNKNOWN,
) {

    companion object {

        @JvmStatic val EMPTY = GuideCalibration()
    }
}
