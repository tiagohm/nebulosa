package nebulosa.api.core

import nebulosa.log.loggerFor
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
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
            val channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)
            // Attempt to acquire an exclusive lock
            lock = channel.tryLock(0, 1, false)

            if (lock == null) {
                LOG.info("another instance of the application is already running")
                return false
            }

            // Add a shutdown hook to release the lock when the application exits
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    lock?.release()
                    channel.close()
                } catch (e: Throwable) {
                    LOG.error("failed to release lock", e)
                } finally {
                    lock = null
                    lockPath.deleteIfExists()
                }
            })

            return true
        } catch (e: Throwable) {
            LOG.error("failed to acquire lock on file", e)
            return false
        }
    }

    fun write(text: String) {
        lock?.also {
            val buffer = ByteBuffer.wrap(text.encodeToByteArray())
            it.channel().write(buffer, 1)
        }
    }

    fun read(): String {
        return RandomAccessFile(lockPath.toFile(), "r").use {
            val length = it.length().toInt() - 1

            if (length > 0) {
                val buffer = ByteArray(length)
                it.seek(1L)
                String(buffer, 0, it.read(buffer))
            } else {
                ""
            }
        }
    }

    private val LOG = loggerFor<FileLocker>()
}
