package nebulosa.desktop

import ch.qos.logback.classic.Level
import javafx.application.Application
import javafx.scene.text.Font
import nebulosa.io.resource
import nebulosa.io.resourceUrl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    Font.loadFont(resourceUrl("fonts/Material-Design-Icons.ttf")!!.toExternalForm(), 22.0)
    Font.loadFont(resource("fonts/Roboto-Regular.ttf"), 12.0)
    Font.loadFont(resource("fonts/Roboto-Bold.ttf"), 12.0)

    System.setProperty("prism.lcdtext", "false")

    // Log level.
    with(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger) {
        level = if ("-v" in args) Level.DEBUG else Level.INFO
    }

    // Run the JavaFX application.
    Application.launch(Nebulosa::class.java, *args)
}
