package nebulosa.desktop.data

import jakarta.persistence.*
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.AxisSize
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType

@Entity
@Table(name = "dsos")
class DeepSkyObjectEntity : SkyObject, AxisSize {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    override var id = 0

    @Column(name = "names", nullable = false, columnDefinition = "TEXT")
    override var names = ""

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
    override var majorAxis = Angle.ZERO

    @Column(name = "minorAxis", nullable = false, columnDefinition = "REAL")
    override var minorAxis = Angle.ZERO

    @Column(name = "orientation", nullable = false, columnDefinition = "REAL")
    override var orientation = Angle.ZERO

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
        if (m != other.m) return false
        if (ngc != other.ngc) return false
        if (ic != other.ic) return false
        if (c != other.c) return false
        if (b != other.b) return false
        if (sh2 != other.sh2) return false
        if (vdb != other.vdb) return false
        if (rcw != other.rcw) return false
        if (ldn != other.ldn) return false
        if (lbn != other.lbn) return false
        if (cr != other.cr) return false
        if (mel != other.mel) return false
        if (pgc != other.pgc) return false
        if (ugc != other.ugc) return false
        if (arp != other.arp) return false
        if (vv != other.vv) return false
        if (dwb != other.dwb) return false
        if (tr != other.tr) return false
        if (st != other.st) return false
        if (ru != other.ru) return false
        if (vdbha != other.vdbha) return false
        if (ced != other.ced) return false
        if (pk != other.pk) return false
        if (png != other.png) return false
        if (snrg != other.snrg) return false
        if (aco != other.aco) return false
        if (hcg != other.hcg) return false
        if (eso != other.eso) return false
        if (vdbh != other.vdbh) return false
        if (mType != other.mType) return false
        if (magnitude != other.magnitude) return false
        if (rightAscension != other.rightAscension) return false
        if (declination != other.declination) return false
        if (type != other.type) return false
        if (redshift != other.redshift) return false
        if (parallax != other.parallax) return false
        if (radialVelocity != other.radialVelocity) return false
        if (distance != other.distance) return false
        if (majorAxis != other.majorAxis) return false
        if (minorAxis != other.minorAxis) return false
        if (orientation != other.orientation) return false
        if (pmRA != other.pmRA) return false
        if (pmDEC != other.pmDEC) return false
        return constellation == other.constellation
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + names.hashCode()
        result = 31 * result + m
        result = 31 * result + ngc
        result = 31 * result + ic
        result = 31 * result + c
        result = 31 * result + b
        result = 31 * result + sh2
        result = 31 * result + vdb
        result = 31 * result + rcw
        result = 31 * result + ldn
        result = 31 * result + lbn
        result = 31 * result + cr
        result = 31 * result + mel
        result = 31 * result + pgc
        result = 31 * result + ugc
        result = 31 * result + arp
        result = 31 * result + vv
        result = 31 * result + dwb
        result = 31 * result + tr
        result = 31 * result + st
        result = 31 * result + ru
        result = 31 * result + vdbha
        result = 31 * result + (ced?.hashCode() ?: 0)
        result = 31 * result + (pk?.hashCode() ?: 0)
        result = 31 * result + (png?.hashCode() ?: 0)
        result = 31 * result + (snrg?.hashCode() ?: 0)
        result = 31 * result + (aco?.hashCode() ?: 0)
        result = 31 * result + (hcg?.hashCode() ?: 0)
        result = 31 * result + (eso?.hashCode() ?: 0)
        result = 31 * result + (vdbh?.hashCode() ?: 0)
        result = 31 * result + (mType?.hashCode() ?: 0)
        result = 31 * result + magnitude.hashCode()
        result = 31 * result + rightAscension.hashCode()
        result = 31 * result + declination.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + redshift.hashCode()
        result = 31 * result + parallax.hashCode()
        result = 31 * result + radialVelocity.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + majorAxis.hashCode()
        result = 31 * result + minorAxis.hashCode()
        result = 31 * result + orientation.hashCode()
        result = 31 * result + pmRA.hashCode()
        result = 31 * result + pmDEC.hashCode()
        result = 31 * result + constellation.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeepSkyObjectEntity(id=$id, names='$names', m=$m, ngc=$ngc, ic=$ic, c=$c," +
                " b=$b, sh2=$sh2, vdb=$vdb, rcw=$rcw, ldn=$ldn, lbn=$lbn, cr=$cr, mel=$mel," +
                " pgc=$pgc, ugc=$ugc, arp=$arp, vv=$vv, dwb=$dwb, tr=$tr, st=$st, ru=$ru," +
                " vdbha=$vdbha, ced=$ced, pk=$pk, png=$png, snrg=$snrg, aco=$aco, hcg=$hcg," +
                " eso=$eso, vdbh=$vdbh, mType=$mType, magnitude=$magnitude," +
                " rightAscension=$rightAscension, declination=$declination, type=$type," +
                " redshift=$redshift, parallax=$parallax, radialVelocity=$radialVelocity," +
                " distance=$distance, majorAxis=$majorAxis, minorAxis=$minorAxis," +
                " orientation=$orientation, pmRA=$pmRA, pmDEC=$pmDEC, constellation=$constellation)"
    }
}
