package nebulosa.desktop.model

import org.jetbrains.exposed.sql.Table

object Names : Table("names") {
    val id = integer("id")
    val name = text("name")
    val dso = (integer("dso") references DeepSkyObjects.id).nullable()
    val star = (integer("star") references Stars.id).nullable()

    override val primaryKey = PrimaryKey(id, name = "pk_names_id")
}
