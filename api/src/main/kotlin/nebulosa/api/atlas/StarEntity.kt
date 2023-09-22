package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.persistence.*
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "stars")
data class StarEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @Column(name = "hd", columnDefinition = "INT4") var hd: Int = 0,
    @Column(name = "hr", columnDefinition = "INT4") var hr: Int = 0,
    @Column(name = "hip", columnDefinition = "INT4") var hip: Int = 0,
    @Column(name = "names", columnDefinition = "TEXT") var names: String = "",
    @Column(name = "magnitude", columnDefinition = "REAL") var magnitude: Double = Double.MAX_VALUE,
    @Column(name = "right_ascension", columnDefinition = "REAL") @field:JsonAlias("rightAscension") var rightAscensionJ2000: Double = 0.0,
    @Column(name = "declination", columnDefinition = "REAL") @field:JsonAlias("declination") var declinationJ2000: Double = 0.0,
    @Column(name = "type", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @Column(name = "sp_type", columnDefinition = "TEXT") var spType: String? = null,
    @Column(name = "redshift", columnDefinition = "REAL") var redshift: Double = 0.0,
    @Column(name = "parallax", columnDefinition = "REAL") var parallax: Double = 0.0,
    @Column(name = "radial_velocity", columnDefinition = "REAL") var radialVelocity: Double = 0.0,
    @Column(name = "distance", columnDefinition = "REAL") var distance: Double = 0.0,
    @Column(name = "pm_ra", columnDefinition = "REAL") var pmRA: Double = 0.0,
    @Column(name = "pm_dec", columnDefinition = "REAL") var pmDEC: Double = 0.0,
    @Column(name = "constellation", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    var constellation: Constellation = Constellation.AND,
) : Persistable<Long> {

    override fun getId() = id

    override fun isNew() = true
}
