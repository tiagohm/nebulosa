import java.io.File
import java.util.*

internal fun createFile(): File {
    val name = UUID.randomUUID().toString()
    return File.createTempFile(name, ".dat")
        .also { it.deleteOnExit() }
}
