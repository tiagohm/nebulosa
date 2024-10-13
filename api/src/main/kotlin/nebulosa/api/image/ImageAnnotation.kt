package nebulosa.api.image

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.converters.angle.DeclinationSerializer
import nebulosa.api.converters.angle.RightAscensionSerializer
import nebulosa.math.Angle
import nebulosa.math.Point2D
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType

data class ImageAnnotation(
    override val x: Double,
    override val y: Double,
    @JvmField val star: StarDSO? = null,
    @JvmField val dso: StarDSO? = null,
    @JvmField val minorPlanet: MinorPlanet? = null,
) : Point2D {

    data class StarDSO(
        override val id: Long = 0L,
        override val name: String,
        override val type: SkyObjectType = SkyObjectType.STAR,
        @field:JsonSerialize(using = RightAscensionSerializer::class) override val rightAscensionJ2000: Angle = 0.0,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val declinationJ2000: Angle = 0.0,
        override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
        override val pmRA: Angle = 0.0,
        override val pmDEC: Angle = 0.0,
        override val parallax: Angle = 0.0,
        override val radialVelocity: Velocity = 0.0,
        override val redshift: Double = 0.0,
        override val constellation: Constellation = Constellation.AND,
    ) : DeepSkyObject {

        constructor(dso: DeepSkyObject) : this(
            dso.id, dso.name, dso.type, dso.rightAscensionJ2000, dso.declinationJ2000,
            dso.magnitude, dso.pmRA, dso.pmDEC, dso.parallax, dso.radialVelocity, dso.redshift,
            dso.constellation
        )
    }

    data class MinorPlanet(
        override val id: Long = 0L,
        override val name: String = "",
        @field:JsonSerialize(using = RightAscensionSerializer::class) override val rightAscensionJ2000: Angle = 0.0,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val declinationJ2000: Angle = 0.0,
        override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
        @JvmField val constellation: Constellation = Constellation.find(ICRF.equatorial(rightAscensionJ2000, declinationJ2000)),
        @JvmField val type: String = "MINOR_PLANET",
    ) : SkyObject
}
