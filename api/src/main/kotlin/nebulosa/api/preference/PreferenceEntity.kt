package nebulosa.api.preference

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

data class PreferenceEntity(
    @JvmField var key: String = "",
    @JvmField var value: String? = null,
) {

    fun mapTo(builder: UpdateBuilder<Int>, update: Boolean = false) {
        if (!update) builder[PreferenceTable.key] = key
        builder[PreferenceTable.value] = value
    }

    companion object {

        fun from(row: ResultRow) = PreferenceEntity(
            row[PreferenceTable.key],
            row[PreferenceTable.value],
        )
    }
}
