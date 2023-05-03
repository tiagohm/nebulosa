package nebulosa.guiding

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad

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

    constructor(data: Map<String, String>) : this(
        data["xRate"]!!.toDouble(),
        data["yRate"]!!.toDouble(),
        data["xAngle"]!!.toDouble().rad,
        data["yAngle"]!!.toDouble().rad,
        data["declination"]!!.toDouble().rad,
        data["rotatorAngle"]!!.toDouble().rad,
        data["binning"]!!.toInt(),
        data["pierSideAtEast"]!!.toBoolean(),
        GuideParity.valueOf(data["raGuideParity"]!!),
        GuideParity.valueOf(data["decGuideParity"]!!),
    )

    fun toMap() = mapOf(
        "xRate" to "$xRate",
        "yRate" to "$yRate",
        "xAngle" to "${xAngle.value}",
        "yAngle" to "${yAngle.value}",
        "declination" to "${declination.value}",
        "rotatorAngle" to "${rotatorAngle.value}",
        "binning" to "$binning",
        "pierSideAtEast" to "$pierSideAtEast",
        "raGuideParity" to "$raGuideParity",
        "decGuideParity" to "$decGuideParity",
    )

    companion object {

        @JvmStatic val EMPTY = Calibration()
    }
}
