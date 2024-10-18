package nebulosa.api.preference

import org.jetbrains.exposed.sql.Table

object PreferenceTable : Table("PREFERENCES") {
    val key = text("KEY")
    val value = text("VALUE").nullable()

    override val primaryKey = PrimaryKey(key)
}
