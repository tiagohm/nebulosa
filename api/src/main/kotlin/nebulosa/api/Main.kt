package nebulosa.api

import com.github.rvesse.airline.SingleCommand
import com.sun.jna.Platform
import nebulosa.api.inject.koin
import nebulosa.time.SystemClock
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.*

fun initAppDirectory(): Path {
    val appPath = when {
        Platform.isLinux() -> Path(System.getProperty("user.home"), ".nebulosa")
        Platform.isWindows() -> Path(FileSystemView.getFileSystemView().defaultDirectory.path, "Nebulosa")
        else -> throw IllegalStateException("unsupported operating system")
    }

    return appPath
        .createDirectories()
        .also { System.setProperty("app.dir", "$it") }
}

private fun Path.clearLogIfPastDays(days: Long = 7L) {
    if (exists()) {
        val pastDays = LocalDate.now(SystemClock).minusDays(days)

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
    val appDir = initAppDirectory()

    koin.modules(module {
        single(named("appDir")) { appDir }
        single(named("logsDir")) { Path("$appDir", "logs").createDirectories().clearLogIfPastDays() }
        single(named("dataDir")) { Path("$appDir", "data").createDirectories() }
    })

    // Sets default locale to en_US.
    Locale.setDefault(Locale.ENGLISH)

    val parser = SingleCommand.singleCommand(Nebulosa::class.java)
    val nebulosa = parser.parse(*args)

    koin.modules(module {
        single { nebulosa }
    })

    nebulosa.run()
}
