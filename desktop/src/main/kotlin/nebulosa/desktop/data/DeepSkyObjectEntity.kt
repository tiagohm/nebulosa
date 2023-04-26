package nebulosa.desktop.data

import jakarta.persistence.*
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import java.io.Serializable

@Entity
@Table(name = "dsos")
class DeepSkyObjectEntity : SkyObject, Serializable {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    override var id = 0

    @ElementCollection
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    @CollectionTable(name = "names", joinColumns = [JoinColumn(name = "dso")])
    override var names = emptyList<String>()

    @Column(name = "m", nullable = false, columnDefinition = "INTEGER")
    var m = 0

    @Column(name = "ngc", nullable = false, columnDefinition = "INTEGER")
    var ngc = 0

    @Column(name = "ic", nullable = false, columnDefinition = "INTEGER")
    var ic = 0

    @Column(name = "c", nullable = false, columnDefinition = "INTEGER")
    var c = 0

    @Column(name = "b", nullable = false, columnDefinition = "INTEGER")
    var b = 0

    @Column(name = "sh2", nullable = false, columnDefinition = "INTEGER")
    var sh2 = 0

    @Column(name = "vdb", nullable = false, columnDefinition = "INTEGER")
    var vdb = 0

    @Column(name = "rcw", nullable = false, columnDefinition = "INTEGER")
    var rcw = 0

    @Column(name = "ldn", nullable = false, columnDefinition = "INTEGER")
    var ldn = 0

    @Column(name = "lbn", nullable = false, columnDefinition = "INTEGER")
    var lbn = 0

    @Column(name = "cr", nullable = false, columnDefinition = "INTEGER")
    var cr = 0

    @Column(name = "mel", nullable = false, columnDefinition = "INTEGER")
    var mel = 0

    @Column(name = "pgc", nullable = false, columnDefinition = "INTEGER")
    var pgc = 0

    @Column(name = "ugc", nullable = false, columnDefinition = "INTEGER")
    var ugc = 0

    @Column(name = "arp", nullable = false, columnDefinition = "INTEGER")
    var arp = 0

    @Column(name = "vv", nullable = false, columnDefinition = "INTEGER")
    var vv = 0

    @Column(name = "dwb", nullable = false, columnDefinition = "INTEGER")
    var dwb = 0

    @Column(name = "tr", nullable = false, columnDefinition = "INTEGER")
    var tr = 0

    @Column(name = "st", nullable = false, columnDefinition = "INTEGER")
    var st = 0

    @Column(name = "ru", nullable = false, columnDefinition = "INTEGER")
    var ru = 0

    @Column(name = "vdbha", nullable = false, columnDefinition = "INTEGER")
    var vdbha = 0

    @Column(name = "ced", columnDefinition = "TEXT")
    var ced: String? = null

    @Column(name = "pk", columnDefinition = "TEXT")
    var pk: String? = null

    @Column(name = "png", columnDefinition = "TEXT")
    var png: String? = null

    @Column(name = "snrg", columnDefinition = "TEXT")
    var snrg: String? = null

    @Column(name = "aco", columnDefinition = "TEXT")
    var aco: String? = null

    @Column(name = "hcg", columnDefinition = "TEXT")
    var hcg: String? = null

    @Column(name = "eso", columnDefinition = "TEXT")
    var eso: String? = null

    @Column(name = "vdbh", columnDefinition = "TEXT")
    var vdbh: String? = null

    @Column(name = "mType", columnDefinition = "TEXT")
    var mType: String? = null

    @Column(name = "magnitude", nullable = false, columnDefinition = "REAL")
    override var magnitude = Double.MAX_VALUE

    @Column(name = "rightAscension", nullable = false, columnDefinition = "REAL")
    override var rightAscension = Angle.ZERO

    @Column(name = "declination", nullable = false, columnDefinition = "REAL")
    override var declination = Angle.ZERO

    @Column(name = "type", nullable = false, columnDefinition = "INTEGER")
    override var type = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE

    @Column(name = "redshift", nullable = false, columnDefinition = "REAL")
    override var redshift = 0.0

    @Column(name = "parallax", nullable = false, columnDefinition = "REAL")
    override var parallax = Angle.ZERO

    @Column(name = "radialVelocity", nullable = false, columnDefinition = "REAL")
    override var radialVelocity = Velocity.ZERO

    @Column(name = "distance", nullable = false, columnDefinition = "REAL")
    override var distance = 0.0

    @Column(name = "majorAxis", nullable = false, columnDefinition = "REAL")
    var majorAxis = Angle.ZERO

    @Column(name = "minorAxis", nullable = false, columnDefinition = "REAL")
    var minorAxis = Angle.ZERO

    @Column(name = "orientation", nullable = false, columnDefinition = "REAL")
    var orientation = Angle.ZERO

    @Column(name = "pmRA", nullable = false, columnDefinition = "REAL")
    override var pmRA = Angle.ZERO

    @Column(name = "pmDEC", nullable = false, columnDefinition = "REAL")
    override var pmDEC = Angle.ZERO

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "constellation", nullable = false, columnDefinition = "INTEGER")
    override var constellation = Constellation.AND

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeepSkyObjectEntity) return false

        if (id != other.id) return false
        if (names != other.names) return false
        if (magnitude != other.magnitude) return false
        if (rightAscension != other.rightAscension) return false
        if (declination != other.declination) return false
        if (type != other.type) return false
        if (redshift != other.redshift) return false
        if (parallax != other.parallax) return false
        if (radialVelocity != other.radialVelocity) return false
        if (distance != other.distance) return false
        if (pmRA != other.pmRA) return false
        if (pmDEC != other.pmDEC) return false
        return constellation == other.constellation
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + names.hashCode()
        result = 31 * result + magnitude.hashCode()
        result = 31 * result + rightAscension.hashCode()
        result = 31 * result + declination.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + redshift.hashCode()
        result = 31 * result + parallax.hashCode()
        result = 31 * result + radialVelocity.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + pmRA.hashCode()
        result = 31 * result + pmDEC.hashCode()
        result = 31 * result + constellation.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeepSkyObjectEntity(id=$id, names=$names, magnitude=$magnitude, " +
                "rightAscension=$rightAscension, declination=$declination, type=$type, " +
                "redshift=$redshift, parallax=$parallax, radialVelocity=$radialVelocity, " +
                "distance=$distance, pmRA=$pmRA, pmDEC=$pmDEC, constellation=$constellation)"
    }
}
