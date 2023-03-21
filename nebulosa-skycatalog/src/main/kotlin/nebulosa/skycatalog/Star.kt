package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.FixedStar

data class Star(
    override val id: Int = 0,
    override val names: List<String> = emptyList(),
    val hr: String = "",
    val hd: String = "",
    val hip: String = "",
    val sao: String = "",
    override val mB: Double = Double.MAX_VALUE,
    override val mV: Double = Double.MAX_VALUE,
    override val rightAscension: Angle = Angle.ZERO,
    override val declination: Angle = Angle.ZERO,
    val spType: String = "",
    override val redshift: Double = 0.0,
    override val parallax: Angle = Angle.ZERO,
    override val radialVelocity: Velocity = Velocity.ZERO,
    override val distance: Double = 0.0,
    override val pmRA: Angle = Angle.ZERO,
    override val pmDEC: Angle = Angle.ZERO,
    override val type: SkyObjectType = SkyObjectType.STAR,
) : SkyObject {

    @Transient @Volatile private var star: FixedStar? = null

    override val position: FixedStar
        get() {
            if (star == null) star = FixedStar(rightAscension, declination, pmRA, pmDEC, parallax)
            return star!!
        }

    companion object {

        @JvmStatic private val serialVersionUID = 1L
    }
}
