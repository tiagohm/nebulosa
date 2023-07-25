package nebulosa.api.data.enums

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
}
