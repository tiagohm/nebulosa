package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

data class Star(
    override val id: Int = 0,
    override val names: List<String> = emptyList(),
    val hr: String? = null,
    val hd: String? = null,
    val hip: String? = null,
    val sao: String? = null,
    override val mB: Double = Double.MAX_VALUE,
    override val mV: Double = Double.MAX_VALUE,
    override val rightAscension: Angle = Angle.ZERO,
    override val declination: Angle = Angle.ZERO,
    val spType: String? = null,
    override val redshift: Double = 0.0,
    override val parallax: Angle = Angle.ZERO,
    override val radialVelocity: Velocity = Velocity.ZERO,
    override val distance: Double = 0.0,
    override val pmRA: Angle = Angle.ZERO,
    override val pmDEC: Angle = Angle.ZERO,
    override val type: SkyObjectType = SkyObjectType.STAR,
    override val constellation: Constellation = Constellation.AND,
) : SkyObject {

    companion object {

        @JvmStatic private val serialVersionUID = 2L
    }
}
