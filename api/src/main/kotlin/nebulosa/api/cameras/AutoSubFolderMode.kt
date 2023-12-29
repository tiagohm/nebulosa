package nebulosa.api.cameras

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class AutoSubFolderMode {
    OFF,
    NOON,
    MIDNIGHT;

    fun directoryNameAt(dateTime: LocalDateTime): String = when {
        this == NOON -> if (dateTime.hour >= 12) dateTime.minusHours(12).format(DateTimeFormatter.ISO_LOCAL_DATE)
        else dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        this == MIDNIGHT -> dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        else -> ""
    }

    fun pathFor(parentPath: Path, dateTime: LocalDateTime? = null): Path {
        return if (this == OFF) parentPath else Path.of("$parentPath", directoryNameAt(dateTime ?: LocalDateTime.now()))
    }
}
