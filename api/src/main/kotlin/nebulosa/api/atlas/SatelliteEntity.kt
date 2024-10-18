package nebulosa.api.atlas

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

data class SatelliteEntity(
    @JvmField var id: Long = 0L,
    @JvmField var name: String = "",
    @JvmField var tle: String = "",
    @JvmField var groups: List<SatelliteGroupType> = emptyList(),
) {

    fun mapTo(builder: UpdateBuilder<Int>, update: Boolean = false) {
        if (!update) builder[SatelliteTable.id] = id
        builder[SatelliteTable.name] = name
        builder[SatelliteTable.tle] = tle
        builder[SatelliteTable.groups] = groups.map { it.ordinal }
    }

    companion object {

        fun from(row: ResultRow) = SatelliteEntity(
            row[SatelliteTable.id],
            row[SatelliteTable.name],
            row[SatelliteTable.tle],
            row[SatelliteTable.groups].map(SatelliteGroupType.entries::get),
        )
    }
}
