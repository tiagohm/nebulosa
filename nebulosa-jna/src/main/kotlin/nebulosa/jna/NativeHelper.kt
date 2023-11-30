package nebulosa.jna

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import nebulosa.io.transferAndClose
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

const val JNA_DIR = "jna.dir"

inline fun <reified T : Library> LibraryProvider.loadLibrary(libraryDir: Path? = null): T {
    return loadLibrary(T::class.java, libraryDir)
}

fun <T : Library> LibraryProvider.loadLibrary(type: Class<out T>, libraryDir: Path? = null): T {
    val extension = when {
        Platform.RESOURCE_PREFIX.startsWith("linux-") -> ".so"
        else -> ".dll"
    }

    val inputStream = provideStream(Platform.RESOURCE_PREFIX, extension)
        ?: throw IllegalStateException("unsupported operating system: ${Platform.RESOURCE_PREFIX}")

    val outputDir = libraryDir
        ?: System.getProperty(JNA_DIR)?.ifBlank { null }?.let(Path::of)
        ?: Path.of(System.getProperty("java.io.tmpdir"))

    val outputPath = Path.of("$outputDir", libraryName)

    if (!outputPath.exists() || outputPath.fileSize() <= 0L) {
        inputStream.transferAndClose(outputPath.outputStream())
    }

    return Native.load("$outputPath", type)
}
