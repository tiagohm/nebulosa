package nebulosa.desktop.model

import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.Table

object StarEntity : Table("stars"), SkyObjectEntity {
    override val id = integer("id")
    val hr = integer("hr")
    val hd = integer("hd")
    val hip = integer("hip")
    override val magnitude = double("magnitude")
    override val rightAscension = double("rightAscension")
    override val declination = double("declination")
    val spType = text("spType").nullable()
    override val redshift = double("redshift")
    override val parallax = double("parallax")
    override val radialVelocity = double("radialVelocity")
    override val distance = double("distance")
    override val pmRA = double("pmRA")
    override val pmDEC = double("pmDEC")
    override val type = enumeration<SkyObjectType>("type")
    override val constellation = enumeration<Constellation>("constellation")

    override val primaryKey = PrimaryKey(id, name = "pk_stars_id")
}
