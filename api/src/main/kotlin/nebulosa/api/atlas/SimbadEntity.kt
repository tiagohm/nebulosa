package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonIgnore
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import nebulosa.api.beans.converters.database.ConstellationPropertyConverter
import nebulosa.api.beans.converters.database.SkyObjectTypePropertyConverter
import nebulosa.api.database.BoxEntity
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.InstantOfTime

@Entity
data class SimbadEntity(
    @Id(assignable = true) override var id: Long = 0L,
    override var name: String = "",
    @Convert(converter = SkyObjectTypePropertyConverter::class, dbType = Int::class)
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    override var rightAscensionJ2000: Angle = 0.0,
    override var declinationJ2000: Angle = 0.0,
    override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override var pmRA: Angle = 0.0,
    override var pmDEC: Angle = 0.0,
    override var parallax: Angle = 0.0,
    override var radialVelocity: Velocity = 0.0,
    override var redshift: Double = 0.0,
    @Convert(converter = ConstellationPropertyConverter::class, dbType = Int::class) override var constellation: Constellation = Constellation.AND,
) : DeepSkyObject, BoxEntity, Body {

    @delegate:Transient private val star by lazy { FixedStar(rightAscensionJ2000, declinationJ2000, pmRA, pmDEC, parallax, radialVelocity) }

    override val center
        @JsonIgnore get() = 0

    override val target
        @JsonIgnore get() = Int.MIN_VALUE

    override fun observedAt(observer: ICRF) = star.observedAt(observer)

    override fun compute(time: InstantOfTime) = star.compute(time)
}
