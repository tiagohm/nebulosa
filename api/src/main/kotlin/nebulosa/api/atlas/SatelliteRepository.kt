package nebulosa.api.atlas

import nebulosa.api.database.contains
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SatelliteRepository(private val connection: Database) {

    operator fun get(id: Long) = transaction(connection) {
        SatelliteTable
            .selectAll()
            .where { SatelliteTable.id eq id }
            .firstOrNull()
            ?.let(SatelliteEntity::from)
    }

    fun search(text: String? = null, groups: List<SatelliteGroupType> = emptyList(), id: Long = 0L) = transaction(connection) {
        SatelliteTable
            .selectAll()
            .also { if (id > 0L) it.andWhere { SatelliteTable.id eq id } }
            .also { if (!text.isNullOrBlank()) it.andWhere { SatelliteTable.name like "%$text%" } }
            .also {
                if (groups.isNotEmpty()) it.andWhere {
                    var expr = SatelliteTable.groups contains groups[0].ordinal

                    for (i in 1 until groups.size) {
                        expr = expr or (SatelliteTable.groups contains groups[i].ordinal)
                    }

                    expr
                }
            }
            .map(SatelliteEntity::from)
    }

    fun add(entity: SatelliteEntity) = transaction(connection) {
        SatelliteTable
            .insert { entity.mapTo(it) } get SatelliteTable.id
        entity
    }

    fun update(entity: SatelliteEntity) = transaction(connection) {
        SatelliteTable
            .update({ SatelliteTable.id eq entity.id }) { entity.mapTo(it, true) }
        entity
    }

    fun add(entities: Iterable<SatelliteEntity>) = transaction(connection) {
        SatelliteTable.batchInsert(entities, false, false) { it.mapTo(this) }
    }

    fun clear() = transaction(connection) {
        SatelliteTable.deleteAll() > 0
    }
}
