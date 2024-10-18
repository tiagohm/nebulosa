package nebulosa.api.atlas

import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.Table

object SkyObjectTable : Table("SKY_OBJECTS") {
    val id = long("ID").autoIncrement()
    val name = text("NAME")
    val type = enumeration<SkyObjectType>("TYPE")
    val rightAscension = double("RIGHT_ASCENSION")
    val declination = double("DECLINATION")
    val magnitude = double("MAGNITUDE")
    val pmRA = double("PM_RA")
    val pmDEC = double("PM_DEC")
    val parallax = double("PARALLAX")
    val radialVelocity = double("RADIAL_VEL")
    val redshift = double("REDSHIFT")
    val constellation = enumeration<Constellation>("CONSTELLATION")

    override val primaryKey = PrimaryKey(id)
}
