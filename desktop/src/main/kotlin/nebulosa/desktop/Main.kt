package nebulosa.desktop

import javafx.application.Application
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.createDirectories

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

fun main(args: Array<String>) {
    initAppDirectory(getOperatingSystemType())

    // Run the JavaFX application.
    Application.launch(Nebulosa::class.java, *args)
}
