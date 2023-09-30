package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

data class DeepSkyObject(
    override val id: Long = 0L,
    override val name: String = "",
    override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override val rightAscensionJ2000: Angle = 0.0,
    override val declinationJ2000: Angle = 0.0,
    override val type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    override val majorAxis: Angle = 0.0,
    override val minorAxis: Angle = 0.0,
    override val orientation: Angle = 0.0,
    override val pmRA: Angle = 0.0,
    override val pmDEC: Angle = 0.0,
    override val parallax: Angle = 0.0,
    override val radialVelocity: Velocity = 0.0,
    override val redshift: Double = 0.0,
    // override val distance: Double = 0.0, // Distance (Mpc for galaxies, kpc for other objects)
    override val constellation: Constellation = Constellation.AND,
) : SkyObject, OrientedObject
