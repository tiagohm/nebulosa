package nebulosa.api.preference

import org.jetbrains.exposed.sql.Count
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PreferenceRepository(private val connection: Database) {

    operator fun contains(key: String) = transaction(connection) {
        !PreferenceTable
            .select(PreferenceTable.key)
            .where { PreferenceTable.key eq key }
            .empty()
    }

    operator fun get(key: String) = transaction(connection) {
        PreferenceTable
            .selectAll()
            .where { PreferenceTable.key eq key }
            .firstOrNull()
            ?.let(PreferenceEntity::from)
    }

    private val count = Count(PreferenceTable.key)

    val size
        get() = transaction(connection) { PreferenceTable.select(count).first()[count] }

    fun add(entity: PreferenceEntity) = transaction(connection) {
        PreferenceTable.insert { entity.mapTo(it) }
        entity
    }

    fun update(entity: PreferenceEntity) = transaction(connection) {
        PreferenceTable.update({ PreferenceTable.key eq entity.key }) { entity.mapTo(it, true) }
    }

    fun delete(key: String) = transaction(connection) {
        PreferenceTable
            .deleteWhere { PreferenceTable.key eq key } == 1
    }

    fun clear() = transaction(connection) {
        PreferenceTable
            .deleteAll() > 0
    }
}
