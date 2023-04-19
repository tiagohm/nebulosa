package nebulosa.desktop.model

import org.jetbrains.exposed.sql.Table

object Stars : Table("stars") {
    val id = integer("id")
    val hr = text("hr")
    val hd = text("hd")
    val hip = text("hip")
    val sao = text("sao")
    val mB = double("mB")
    val mV = double("mV")
    val rightAscension = double("rightAscension")
    val declination = double("declination")
    val spType = text("spType")
    val redshift = double("redshift")
    val parallax = double("parallax")
    val radialVelocity = double("radialVelocity")
    val distance = double("distance")
    val pmRA = double("pmRA")
    val pmDEC = double("pmDEC")
    val type = text("type")
    val constellation = text("constellation")

    override val primaryKey = PrimaryKey(id, name = "pk_stars_id")
}
