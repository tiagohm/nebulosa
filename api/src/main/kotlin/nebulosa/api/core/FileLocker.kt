package nebulosa.api.core

import nebulosa.log.e
import nebulosa.log.i
import nebulosa.log.loggerFor
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

object FileLocker {

    private val lockPath = Path(System.getProperty("java.io.tmpdir"), "nebulosa.lock")

    @Volatile private var lock: FileLock? = null

    @Synchronized
    fun tryLock(): Boolean {
        if (lock != null) {
            return true
        }

        try {
            val channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            // Attempt to acquire an exclusive lock
            lock = channel.tryLock()

            if (lock == null) {
                LOG.i("another instance of the application is already running")
                return false
            }

            // Add a shutdown hook to release the lock when the application exits
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    lock?.release()
                    channel.close()
                } catch (e: Throwable) {
                    LOG.e("failed to release lock", e)
                } finally {
                    lock = null
                    lockPath.deleteIfExists()
                }
            })

            return true
        } catch (e: Throwable) {
            LOG.e("failed to acquire lock on file", e)
            return false
        }
    }

    fun write(text: String) {
        lock?.channel()?.write(ByteBuffer.wrap(text.encodeToByteArray()), 0)
    }

    fun read(): String {
        return Files.readString(lockPath)
    }

    private val LOG = loggerFor<FileLocker>()
}
