package nebulosa.api.calibration

import nebulosa.api.database.ilike
import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CalibrationFrameRepository(private val connection: Database) {

    fun get(id: Long) = transaction(connection) {
        CalibrationFrameTable
            .selectAll()
            .where { CalibrationFrameTable.id eq id }
            .firstOrNull()
            ?.let(CalibrationFrameEntity::from)
    }

    fun groups() = transaction(connection) {
        CalibrationFrameTable
            .select(CalibrationFrameTable.group)
            .distinct()
            .map { it[CalibrationFrameTable.group] }
            .toSet()
    }

    fun findAll(group: String? = null) = transaction(connection) {
        CalibrationFrameTable
            .selectAll()
            .also { if (!group.isNullOrBlank()) it.where { CalibrationFrameTable.group eq group } }
            .map(CalibrationFrameEntity::from)
    }

    fun delete(group: String, path: String) = transaction(connection) {
        CalibrationFrameTable
            .deleteWhere { (CalibrationFrameTable.group eq group) and (CalibrationFrameTable.path eq path) }
    }

    fun darkFrames(group: String, width: Int, height: Int, bin: Int, exposureTime: Long, gain: Double) = transaction(connection) {
        CalibrationFrameTable
            .selectAll()
            .where { CalibrationFrameTable.type eq FrameType.DARK }
            .andWhere { CalibrationFrameTable.enabled eq true }
            .andWhere { CalibrationFrameTable.group eq group }
            .andWhere { CalibrationFrameTable.width eq width }
            .andWhere { CalibrationFrameTable.height eq height }
            .andWhere { CalibrationFrameTable.binX eq bin }
            .andWhere { CalibrationFrameTable.binY eq bin }
            .also { if (exposureTime > 0L) it.andWhere { CalibrationFrameTable.exposureTime eq exposureTime } }
            .also { if (gain > 0L) it.andWhere { CalibrationFrameTable.gain eq gain } }
            .map(CalibrationFrameEntity::from)
    }

    fun biasFrames(group: String, width: Int, height: Int, bin: Int, gain: Double) = transaction(connection) {
        CalibrationFrameTable
            .selectAll()
            .where { CalibrationFrameTable.type eq FrameType.BIAS }
            .andWhere { CalibrationFrameTable.enabled eq true }
            .andWhere { CalibrationFrameTable.group eq group }
            .andWhere { CalibrationFrameTable.width eq width }
            .andWhere { CalibrationFrameTable.height eq height }
            .andWhere { CalibrationFrameTable.binX eq bin }
            .andWhere { CalibrationFrameTable.binY eq bin }
            .also { if (gain > 0L) it.andWhere { CalibrationFrameTable.gain eq gain } }
            .map(CalibrationFrameEntity::from)
    }

    fun flatFrames(group: String, filter: String?, width: Int, height: Int, bin: Int) = transaction(connection) {
        CalibrationFrameTable
            .selectAll()
            .where { CalibrationFrameTable.type eq FrameType.FLAT }
            .andWhere { CalibrationFrameTable.enabled eq true }
            .andWhere { CalibrationFrameTable.group eq group }
            .andWhere { CalibrationFrameTable.width eq width }
            .andWhere { CalibrationFrameTable.height eq height }
            .andWhere { CalibrationFrameTable.binX eq bin }
            .andWhere { CalibrationFrameTable.binY eq bin }
            .also { it.andWhere { if (filter.isNullOrBlank()) CalibrationFrameTable.filter.isNull() else CalibrationFrameTable.filter ilike filter } }
            .map(CalibrationFrameEntity::from)
    }

    fun save(entity: CalibrationFrameEntity) = transaction(connection) {
        if (entity.id == 0L) {
            entity.id = CalibrationFrameTable.insert {
                it[type] = entity.type
                it[group] = entity.group
                it[filter] = entity.filter
                it[exposureTime] = entity.exposureTime
                it[temperature] = entity.temperature
                it[width] = entity.width
                it[height] = entity.height
                it[binX] = entity.binX
                it[binY] = entity.binY
                it[gain] = entity.gain
                it[path] = "${entity.path}"
                it[enabled] = entity.enabled
            } get CalibrationFrameTable.id
        } else {
            CalibrationFrameTable.update({ CalibrationFrameTable.id eq entity.id }) {
                it[type] = entity.type
                it[group] = entity.group
                it[filter] = entity.filter
                it[exposureTime] = entity.exposureTime
                it[temperature] = entity.temperature
                it[width] = entity.width
                it[height] = entity.height
                it[binX] = entity.binX
                it[binY] = entity.binY
                it[gain] = entity.gain
                it[path] = "${entity.path}"
                it[enabled] = entity.enabled
            }
        }

        entity
    }

    fun delete(id: Long) = transaction(connection) {
        CalibrationFrameTable.deleteWhere { CalibrationFrameTable.id eq id } == 1
    }
}
