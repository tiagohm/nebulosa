package nebulosa.api.tasks

import nebulosa.util.concurrency.cancellation.CancellationToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

abstract class Job : CompletableFuture<Unit>(), Runnable {

    abstract val task: Task

    abstract val name: String

    private val cancellationToken = CancellationToken()
    private val running = AtomicBoolean()

    @Volatile private var thread: Thread? = null

    val isRunning
        get() = running.get()

    final override fun run() {
        try {
            running.set(true)
            task.execute(cancellationToken)
        } finally {
            running.set(false)
            thread = null
            cancellationToken.close()
            complete(Unit)
            task.close()
        }
    }

    /**
     * Runs this Job in a new thread.
     */
    @Synchronized
    fun start() {
        if (thread == null && !running.get()) {
            thread = Thread(this, name)
            thread!!.isDaemon = true
            thread!!.start()
        }
    }

    /**
     * Stops gracefully this Job.
     */
    fun stop() {
        cancellationToken.cancel()
    }

    /**
     * Pauses this Job.
     */
    fun pause() {
        cancellationToken.pause()
    }

    /**
     * Unpauses this Job.
     */
    fun unpause() {
        cancellationToken.unpause()
    }
}
