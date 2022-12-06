package nebulosa.api.cameras

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class AutoSubFolderMode {
    OFF,
    NOON,
    MIDNIGHT;

    fun subFolderName(): String {
        if (this == OFF) return ""

        val now = LocalDateTime.now()

        return if (this == NOON) {
            if (now.hour >= 12) now.minusHours(12).format(FOLDER_DATE_FORMAT)
            else now.format(FOLDER_DATE_FORMAT)
        } else {
            now.format(FOLDER_DATE_FORMAT)
        }
    }

    companion object {

        @JvmStatic
        private val FOLDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
