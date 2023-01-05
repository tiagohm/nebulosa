package nebulosa.desktop.equipments

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

abstract class ThreadedTask<T> : CompletableFuture<T>(), Runnable {

    abstract fun execute(): T

    abstract fun finishGracefully()

    final override fun run() {
        try {
            complete(execute())
        } catch (e: InterruptedException) {
            LOG.info("task was interrupted: {}", this::class.simpleName)
        } catch (e: Throwable) {
            LOG.error("task error", e)
            completeExceptionally(e)
        }
    }

    fun runOnThread(): Thread {
        val thread = Thread(this)
        thread.start()
        return thread
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(ThreadedTask::class.java)
    }
}
