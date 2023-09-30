package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.persistence.*
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.SpectralObject
import nebulosa.time.InstantOfTime
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "stars")
data class StarEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") override var id: Long = 0L,
    @Column(name = "name", columnDefinition = "TEXT") override var name: String = "",
    @Column(name = "magnitude", columnDefinition = "REAL") override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    @Column(name = "right_ascension", columnDefinition = "REAL") @field:JsonAlias("rightAscension")
    override var rightAscensionJ2000: Angle = 0.0,
    @Column(name = "declination", columnDefinition = "REAL") @field:JsonAlias("declination")
    override var declinationJ2000: Angle = 0.0,
    @Column(name = "type", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @Column(name = "sp_type", columnDefinition = "TEXT") override var spType: String = "",
    @Column(name = "pm_ra", columnDefinition = "REAL") override var pmRA: Angle = 0.0,
    @Column(name = "pm_dec", columnDefinition = "REAL") override var pmDEC: Angle = 0.0,
    @Column(name = "parallax", columnDefinition = "REAL") override var parallax: Angle = 0.0,
    @Column(name = "radial_velocity", columnDefinition = "REAL") override var radialVelocity: Velocity = 0.0,
    @Column(name = "redshift", columnDefinition = "REAL") override var redshift: Double = 0.0,
    @Column(name = "distance", columnDefinition = "REAL") var distance: Double = 0.0,
    @Column(name = "constellation", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    override var constellation: Constellation = Constellation.AND,
) : SkyObject, SpectralObject, Body, Persistable<Long> {

    @delegate:Transient private val star by lazy { FixedStar(rightAscensionJ2000, declinationJ2000, pmRA, pmDEC, parallax, radialVelocity) }

    override val center
        get() = 0

    override val target
        get() = Int.MIN_VALUE

    override fun getId() = id

    override fun isNew() = true

    override fun observedAt(observer: ICRF) = star.observedAt(observer)

    override fun compute(time: InstantOfTime) = star.compute(time)
}
