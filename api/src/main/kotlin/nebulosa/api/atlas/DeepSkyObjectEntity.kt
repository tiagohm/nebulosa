package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.OrientedSkyObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.InstantOfTime
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "dsos")
data class DeepSkyObjectEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") override var id: Long = 0L,
    @Column(name = "name", columnDefinition = "TEXT") override var name: String = "",
    @Column(name = "magnitude", columnDefinition = "REAL") override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @Column(name = "right_ascension", columnDefinition = "REAL") @field:JsonAlias("rightAscension")
    override var rightAscensionJ2000: Angle = 0.0,
    @Column(name = "declination", columnDefinition = "REAL") @field:JsonAlias("declination")
    override var declinationJ2000: Angle = 0.0,
    @Column(name = "type", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @Column(name = "major_axis", columnDefinition = "REAL") override var majorAxis: Angle = 0.0,
    @Column(name = "minor_axis", columnDefinition = "REAL") override var minorAxis: Angle = 0.0,
    @Column(name = "orientation", columnDefinition = "REAL") override var orientation: Angle = 0.0,
    @Column(name = "pm_ra", columnDefinition = "REAL") override var pmRA: Angle = 0.0,
    @Column(name = "pm_dec", columnDefinition = "REAL") override var pmDEC: Angle = 0.0,
    @Column(name = "parallax", columnDefinition = "REAL") override var parallax: Angle = 0.0,
    @Column(name = "radial_velocity", columnDefinition = "REAL") override var radialVelocity: Velocity = 0.0,
    @Column(name = "redshift", columnDefinition = "REAL") override var redshift: Double = 0.0,
    @Column(name = "distance", columnDefinition = "REAL") var distance: Double = 0.0,
    @Column(name = "constellation", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    override var constellation: Constellation = Constellation.AND,
) : DeepSkyObject, OrientedSkyObject, Body, Persistable<Long> {

    @delegate:Transient private val star by lazy { FixedStar(rightAscensionJ2000, declinationJ2000, pmRA, pmDEC, parallax, radialVelocity) }

    override val center
        @JsonIgnore get() = 0

    override val target
        @JsonIgnore get() = Int.MIN_VALUE

    override fun getId() = id

    @JsonIgnore
    override fun isNew() = true

    override fun observedAt(observer: ICRF) = star.observedAt(observer)

    override fun compute(time: InstantOfTime) = star.compute(time)
}
