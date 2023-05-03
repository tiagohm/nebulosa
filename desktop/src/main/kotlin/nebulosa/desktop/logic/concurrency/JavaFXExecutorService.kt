package nebulosa.desktop.logic.concurrency

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class JavaFXExecutorService : AbstractExecutorService() {

    @Volatile private var terminated = false

    override fun execute(command: Runnable) {
        if (isShutdown) throw IllegalStateException("Must not call execute() after pool is shut down")
        JavaFXExecutor.execute(command)
    }

    override fun shutdown() {
        terminated = true
    }

    override fun shutdownNow(): List<Runnable> {
        shutdown()
        return emptyList()
    }

    override fun isShutdown() = terminated

    override fun isTerminated() = terminated

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        if (!isShutdown) throw IllegalStateException("Must not call execute() after pool is shut down")
        return true
    }
}
