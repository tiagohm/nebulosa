package nebulosa.api.atlas

import jakarta.persistence.*
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "dsos")
data class DeepSkyObjectEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @Column(name = "names", columnDefinition = "TEXT") var names: String = "",
    @Column(name = "m", columnDefinition = "INT4") var m: Int = 0,
    @Column(name = "ngc", columnDefinition = "INT4") var ngc: Int = 0,
    @Column(name = "ic", columnDefinition = "INT4") var ic: Int = 0,
    @Column(name = "c", columnDefinition = "INT4") var c: Int = 0,
    @Column(name = "b", columnDefinition = "INT4") var b: Int = 0,
    @Column(name = "sh2", columnDefinition = "INT4") var sh2: Int = 0,
    @Column(name = "vdb", columnDefinition = "INT4") var vdb: Int = 0,
    @Column(name = "rcw", columnDefinition = "INT4") var rcw: Int = 0,
    @Column(name = "ldn", columnDefinition = "INT4") var ldn: Int = 0,
    @Column(name = "lbn", columnDefinition = "INT4") var lbn: Int = 0,
    @Column(name = "cr", columnDefinition = "INT4") var cr: Int = 0,
    @Column(name = "mel", columnDefinition = "INT4") var mel: Int = 0,
    @Column(name = "pgc", columnDefinition = "INT4") var pgc: Int = 0,
    @Column(name = "ugc", columnDefinition = "INT4") var ugc: Int = 0,
    @Column(name = "arp", columnDefinition = "INT4") var arp: Int = 0,
    @Column(name = "vv", columnDefinition = "INT4") var vv: Int = 0,
    @Column(name = "dwb", columnDefinition = "INT4") var dwb: Int = 0,
    @Column(name = "tr", columnDefinition = "INT4") var tr: Int = 0,
    @Column(name = "st", columnDefinition = "INT4") var st: Int = 0,
    @Column(name = "ru", columnDefinition = "INT4") var ru: Int = 0,
    @Column(name = "vdbha", columnDefinition = "INT4") var vdbha: Int = 0,
    @Column(name = "ced", columnDefinition = "TEXT") var ced: String? = null,
    @Column(name = "pk", columnDefinition = "TEXT") var pk: String? = null,
    @Column(name = "png", columnDefinition = "TEXT") var png: String? = null,
    @Column(name = "snrg", columnDefinition = "TEXT") var snrg: String? = null,
    @Column(name = "aco", columnDefinition = "TEXT") var aco: String? = null,
    @Column(name = "hcg", columnDefinition = "TEXT") var hcg: String? = null,
    @Column(name = "eso", columnDefinition = "TEXT") var eso: String? = null,
    @Column(name = "vdbh", columnDefinition = "TEXT") var vdbh: String? = null,
    @Column(name = "m_type", columnDefinition = "TEXT") var mType: String? = null,
    @Column(name = "magnitude", columnDefinition = "REAL") var magnitude: Double = Double.MAX_VALUE,
    @Column(name = "right_ascension", columnDefinition = "REAL") var rightAscensionJ2000: Double = 0.0,
    @Column(name = "declination", columnDefinition = "REAL") var declinationJ2000: Double = 0.0,
    @Column(name = "type", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    @Column(name = "redshift", columnDefinition = "REAL") var redshift: Double = 0.0,
    @Column(name = "parallax", columnDefinition = "REAL") var parallax: Double = 0.0,
    @Column(name = "radial_velocity", columnDefinition = "REAL") var radialVelocity: Double = 0.0,
    @Column(name = "distance", columnDefinition = "REAL") var distance: Double = 0.0,
    @Column(name = "major_axis", columnDefinition = "REAL") var majorAxis: Double = 0.0,
    @Column(name = "minor_axis", columnDefinition = "REAL") var minorAxis: Double = 0.0,
    @Column(name = "orientation", columnDefinition = "REAL") var orientation: Double = 0.0,
    @Column(name = "pm_ra", columnDefinition = "REAL") var pmRA: Double = 0.0,
    @Column(name = "pm_dec", columnDefinition = "REAL") var pmDEC: Double = 0.0,
    @Column(name = "constellation", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL)
    var constellation: Constellation = Constellation.AND,
) : Persistable<Long> {

    override fun getId() = id

    override fun isNew() = true
}
