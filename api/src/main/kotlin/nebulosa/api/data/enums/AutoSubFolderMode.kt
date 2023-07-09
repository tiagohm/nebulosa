package nebulosa.api.data.enums

import io.objectbox.converter.PropertyConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class AutoSubFolderMode {
    OFF,
    NOON,
    MIDNIGHT;

    fun nameAt(dateTime: LocalDateTime): String {
        return when {
            this == NOON -> if (dateTime.hour >= 12) dateTime.minusHours(12).format(DateTimeFormatter.ISO_LOCAL_DATE)
            else dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
            this == MIDNIGHT -> dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> ""
        }
    }

    class BoxConverter : PropertyConverter<AutoSubFolderMode?, String?> {

        override fun convertToEntityProperty(databaseValue: String?): AutoSubFolderMode {
            return databaseValue?.let(::valueOf) ?: OFF
        }

        override fun convertToDatabaseValue(entityProperty: AutoSubFolderMode?): String {
            return entityProperty?.name ?: "OFF"
        }
    }
}
