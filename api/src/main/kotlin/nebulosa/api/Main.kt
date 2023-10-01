package nebulosa.api

import org.springframework.boot.runApplication
import oshi.PlatformEnum
import oshi.SystemInfo
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

fun initAppDirectory(): Path? {
    val appPath = when (SystemInfo.getCurrentPlatform()) {
        PlatformEnum.LINUX -> {
            val userHomeDir = Path.of(System.getProperty("user.home"))
            Path.of("$userHomeDir", ".nebulosa")
        }
        PlatformEnum.WINDOWS -> {
            val documentsDir = FileSystemView.getFileSystemView().defaultDirectory.path
            Path.of(documentsDir, "Nebulosa")
        }
        else -> return null
    }

    appPath.createDirectories()

    System.setProperty("app.dir", "$appPath")

    return appPath
}

private fun Path.clearLogIfPastDays(days: Long = 7L) {
    if (exists()) {
        val pastDays = LocalDate.now().minusDays(days)

        for (entry in listDirectoryEntries("nebulosa-*.log")) {
            val logDate = entry.fileName.toString()
                .replace("nebulosa-", "")
                .replace(".log", "")
                .let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
                ?: continue

            if (pastDays.isAfter(logDate)) {
                entry.deleteIfExists()
            }
        }
    }
}

fun main(args: Array<String>) {
    with(initAppDirectory()) {
        Path.of("$this", "logs").createDirectories().clearLogIfPastDays()
        Path.of("$this", "data").createDirectories().also { System.setProperty("DATA_PATH", "$it") }
    }

    // Sets default locale to en_US.
    Locale.setDefault(Locale.ENGLISH)

    // Run the Spring Boot application.
    runApplication<Nebulosa>(*args)
}
