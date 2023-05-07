package nebulosa.desktop.data

import jakarta.persistence.*
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType

@Entity
@Table(name = "stars")
class StarEntity : SkyObject {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    override var id = 0

    @Column(name = "hd", columnDefinition = "INTEGER")
    var hd = 0

    @Column(name = "hr", columnDefinition = "INTEGER")
    var hr = 0

    @Column(name = "hip", columnDefinition = "INTEGER")
    var hip = 0

    @Column(name = "names", nullable = false, columnDefinition = "TEXT")
    override var names = ""

    @Column(name = "magnitude", nullable = false, columnDefinition = "REAL")
    override var magnitude = Double.MAX_VALUE

    @Column(name = "rightAscension", nullable = false, columnDefinition = "REAL")
    override var rightAscension = Angle.ZERO

    @Column(name = "declination", nullable = false, columnDefinition = "REAL")
    override var declination = Angle.ZERO

    @Column(name = "type", nullable = false, columnDefinition = "INTEGER")
    override var type = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE

    @Column(name = "spType", columnDefinition = "TEXT")
    var spType: String? = null

    @Column(name = "redshift", nullable = false, columnDefinition = "REAL")
    override var redshift = 0.0

    @Column(name = "parallax", nullable = false, columnDefinition = "REAL")
    override var parallax = Angle.ZERO

    @Column(name = "radialVelocity", nullable = false, columnDefinition = "REAL")
    override var radialVelocity: Velocity = Velocity.ZERO

    @Column(name = "distance", nullable = false, columnDefinition = "REAL")
    override var distance = 0.0

    @Column(name = "pmRA", nullable = false, columnDefinition = "REAL")
    override var pmRA = Angle.ZERO

    @Column(name = "pmDEC", nullable = false, columnDefinition = "REAL")
    override var pmDEC = Angle.ZERO

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "constellation", nullable = false, columnDefinition = "INTEGER")
    override var constellation = Constellation.AND

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarEntity) return false

        if (id != other.id) return false
        if (hd != other.hd) return false
        if (hr != other.hr) return false
        if (hip != other.hip) return false
        if (names != other.names) return false
        if (magnitude != other.magnitude) return false
        if (rightAscension != other.rightAscension) return false
        if (declination != other.declination) return false
        if (type != other.type) return false
        if (spType != other.spType) return false
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
        result = 31 * result + hd
        result = 31 * result + hr
        result = 31 * result + hip
        result = 31 * result + names.hashCode()
        result = 31 * result + magnitude.hashCode()
        result = 31 * result + rightAscension.hashCode()
        result = 31 * result + declination.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + spType.hashCode()
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
        return "StarEntity(id=$id, hd=$hd, hr=$hr, hip=$hip, names=$names, " +
                "magnitude=$magnitude, rightAscension=$rightAscension, declination=$declination, " +
                "type=$type, spType='$spType', redshift=$redshift, parallax=$parallax, " +
                "radialVelocity=$radialVelocity, distance=$distance, pmRA=$pmRA, pmDEC=$pmDEC, " +
                "constellation=$constellation)"
    }
}
