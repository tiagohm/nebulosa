package nebulosa.simbad

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.*
import nebulosa.time.InstantOfTime

data class SimbadEntry(
    override var id: Long = 0L,
    override val name: List<String> = emptyList(),
    override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override var rightAscensionJ2000: Angle = 0.0,
    override var declinationJ2000: Angle = 0.0,
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    override val spType: String = "",
    override var majorAxis: Angle = 0.0,
    override var minorAxis: Angle = 0.0,
    override var orientation: Angle = 0.0,
    override var pmRA: Angle = 0.0,
    override var pmDEC: Angle = 0.0,
    override var parallax: Angle = 0.0,
    override var radialVelocity: Velocity = 0.0,
    override var redshift: Double = 0.0,
    var distance: Distance = 0.0,
    override var constellation: Constellation = Constellation.AND,
) : DeepSkyObject, SpectralSkyObject, OrientedSkyObject, Body {

    @delegate:Transient private val star by lazy { FixedStar(rightAscensionJ2000, declinationJ2000, pmRA, pmDEC, parallax, radialVelocity) }

    override val center
        @JsonIgnore get() = 0

    override val target
        @JsonIgnore get() = Int.MIN_VALUE

    override fun observedAt(observer: ICRF) = star.observedAt(observer)

    override fun compute(time: InstantOfTime) = star.compute(time)
}
