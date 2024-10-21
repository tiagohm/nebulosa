package nebulosa.test

import org.junit.jupiter.api.AfterEach
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isDirectory

abstract class AbstractTest {

    private val autoCloseables = HashSet<AutoCloseable>(2)
    private val deletablePaths = ArrayList<Path>(2)

    @AfterEach
    open fun afterEach() {
        closeResourcesAfterEach()
        deletePathsAfterEach()
    }

    private fun closeResourcesAfterEach() {
        autoCloseables.forEach(AutoCloseable::close)
        autoCloseables.clear()
    }

    private fun deletePathsAfterEach() {
        deletablePaths.forEach { it.deleteRecursivelyOrIfExists() }
        deletablePaths.clear()
    }

    protected fun <T : AutoCloseable> T.autoClose(): T {
        return apply(autoCloseables::add)
    }

    protected fun Path.deleteRecursivelyOrIfExists() {
        if (isDirectory()) deleteRecursively() else deleteIfExists()
    }

    protected fun Path.deleteAfterEach(): Path {
        return apply(deletablePaths::add)
    }

    protected fun tempPath(prefix: String, suffix: String): Path {
        return Files.createTempFile(prefix, suffix).deleteAfterEach()
    }

    protected fun tempDirectory(prefix: String): Path {
        return Files.createTempDirectory(prefix).deleteAfterEach()
    }
}
