package nebulosa.desktop.view.camera

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class AutoSubFolderMode {
    OFF,
    NOON,
    MIDNIGHT;

    fun folderName(time: LocalDateTime = LocalDateTime.now()): String {
        return when {
            this == NOON -> if (time.hour >= 12) time.minusHours(12)
                .format(DateTimeFormatter.ISO_LOCAL_DATE) else time.format(DateTimeFormatter.ISO_LOCAL_DATE)
            this == MIDNIGHT -> time.format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> ""
        }
    }
}
