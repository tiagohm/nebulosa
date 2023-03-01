package nebulosa.desktop

import javafx.application.Application
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeBytes

enum class OperatingSystemType {
    WINDOWS,
    LINUX,
    MAC,
    OTHER,
}

fun getOperatingSystemType(): OperatingSystemType {
    val osName = System.getProperty("os.name", "generic").lowercase()

    return when {
        "mac" in osName || "darwin" in osName -> OperatingSystemType.MAC
        "win" in osName -> OperatingSystemType.WINDOWS
        "nux" in osName -> OperatingSystemType.LINUX
        else -> OperatingSystemType.OTHER
    }
}

fun initAppDirectory(operatingSystemType: OperatingSystemType): Path? {
    val appDirectory = when (operatingSystemType) {
        OperatingSystemType.LINUX -> {
            val userHomeDir = Paths.get(System.getProperty("user.home"))
            Paths.get("$userHomeDir", ".nebulosa")
        }
        OperatingSystemType.WINDOWS -> {
            val documentsDir = FileSystemView.getFileSystemView().defaultDirectory.path
            Paths.get(documentsDir, "Nebulosa")
        }
        else -> {
            null
        }
    }

    appDirectory?.createDirectories()

    System.setProperty("app.dir", "$appDirectory")

    return appDirectory
}

private fun clearLogIfPastDays(days: Long = 7L) {
    val logPath = Paths.get(System.getProperty("app.dir"), "nebulosa.log")

    if (logPath.exists()) {
        val dateTimeRegex = Regex("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        for (line in logPath.bufferedReader().lines()) {
            val dateTimeText = dateTimeRegex.matchEntire(line)?.groupValues?.get(1) ?: continue
            val dateTime = LocalDateTime.parse(dateTimeText, dateTimeFormatter)

            if (LocalDateTime.now().minusDays(days).isAfter(dateTime)) {
                logPath.writeBytes(ByteArray(0))
            }

            break
        }
    }
}

fun main(args: Array<String>) {
    initAppDirectory(getOperatingSystemType())

    clearLogIfPastDays()

    // Run the JavaFX application.
    Application.launch(Nebulosa::class.java, *args)
}
