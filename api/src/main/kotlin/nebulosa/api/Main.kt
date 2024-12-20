package nebulosa.api

import com.github.rvesse.airline.SingleCommand
import com.sun.jna.Platform
import java.nio.file.Path
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

const val APP_DIR_KEY = "app.dir"

fun initAppDirectory(): Path {
    val appPath = when {
        Platform.isLinux() -> Path(System.getProperty("user.home"), ".nebulosa")
        Platform.isWindows() -> Path(FileSystemView.getFileSystemView().defaultDirectory.path, "Nebulosa")
        else -> throw IllegalStateException("unsupported operating system")
    }

    return appPath
        .createDirectories()
        .also { System.setProperty(APP_DIR_KEY, "$it") }
}

fun main(args: Array<String>) {
    initAppDirectory()

    // Sets default locale to en_US.
    Locale.setDefault(Locale.ENGLISH)

    val parser = SingleCommand.singleCommand(Nebulosa::class.java)
    val nebulosa = parser.parse(*args)
    nebulosa.run()
}
