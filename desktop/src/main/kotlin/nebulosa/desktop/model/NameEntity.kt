package nebulosa.desktop.model

import org.jetbrains.exposed.sql.Table

object NameEntity : Table("names") {
    val id = integer("id")
    val name = text("name")
    val dso = (integer("dso") references DsoEntity.id).nullable()
    val star = (integer("star") references StarEntity.id).nullable()

    override val primaryKey = PrimaryKey(id, name = "pk_names_id")
}
