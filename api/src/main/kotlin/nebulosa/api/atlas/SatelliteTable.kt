package nebulosa.api.atlas

import org.jetbrains.exposed.sql.Table

object SatelliteTable : Table("SATELLITES") {
    val id = long("ID")
    val name = text("NAME")
    val tle = text("TLE")
    val groups = array<Int>("GROUPS")

    override val primaryKey = PrimaryKey(id)
}
