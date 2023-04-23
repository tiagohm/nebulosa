package nebulosa.desktop

import javafx.application.Application
import javafx.scene.text.Font
import nebulosa.io.resource
import oshi.PlatformEnum
import oshi.SystemInfo
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

fun initAppDirectory(): Path? {
    val appDirectory = when (SystemInfo.getCurrentPlatform()) {
        PlatformEnum.LINUX -> {
            val userHomeDir = Paths.get(System.getProperty("user.home"))
            Paths.get("$userHomeDir", ".nebulosa")
        }
        PlatformEnum.WINDOWS -> {
            val documentsDir = FileSystemView.getFileSystemView().defaultDirectory.path
            Paths.get(documentsDir, "Nebulosa")
        }
        else -> return null
    }

    appDirectory.createDirectories()

    System.setProperty("app.dir", "$appDirectory")

    return appDirectory
}

private fun Path.clearLogIfPastDays(days: Long = 7L) {
    if (exists()) {
        val pastDays = LocalDate.now().minusDays(days)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (entry in listDirectoryEntries("nebulosa-*.log")) {
            val logDate = entry.fileName.toString()
                .replace("nebulosa-", "")
                .replace(".log", "")
                .let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }
                ?: continue

            if (pastDays.isAfter(logDate)) {
                entry.deleteIfExists()
            }
        }
    }
}

fun main(args: Array<String>) {
    with(initAppDirectory()) {
        Paths.get("$this", "logs").createDirectories().clearLogIfPastDays()
    }

    // Sets default locale to en_US.
    Locale.setDefault(Locale.ENGLISH)

    // Fonts.
    resource("fonts/Material-Design-Icons.ttf").use { Font.loadFont(it, 22.0) }
    resource("fonts/Roboto-Regular.ttf").use { Font.loadFont(it, 12.0) }
    resource("fonts/Roboto-Bold.ttf").use { Font.loadFont(it, 12.0) }

    System.setProperty("prism.lcdtext", "false")

    // Run the JavaFX application.
    Application.launch(Nebulosa::class.java, *args)
}
