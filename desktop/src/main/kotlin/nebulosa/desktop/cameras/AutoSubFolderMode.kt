package nebulosa.desktop.cameras;

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class AutoSubFolderMode {
    OFF,
    NOON,
    MIDNIGHT;

    fun folderName(time: LocalDateTime = LocalDateTime.now()): String {
        return when {
            this == NOON -> if (time.hour >= 12) time.minusHours(12).format(DATE_FORMAT) else time.format(DATE_FORMAT)
            this == MIDNIGHT -> time.format(DATE_FORMAT)
            else -> ""
        }
    }

    companion object {

        @JvmStatic private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
