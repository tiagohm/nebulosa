package nebulosa.api.tasks

import nebulosa.common.concurrency.cancel.CancellationToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

abstract class Job : CompletableFuture<Unit>(), Runnable {

    abstract val task: Task<*>

    abstract val name: String

    private val cancellationToken = CancellationToken()

    @Volatile private var thread: Thread? = null

    final override fun run() {
        try {
            task.execute(cancellationToken)
        } finally {
            thread = null
            cancellationToken.close()
            complete(Unit)
            task.close()
        }
    }

    /**
     * Runs this Job in a new thread.
     */
    fun start() {
        thread = Thread(this, name)
        thread!!.isDaemon = false
        thread!!.start()
    }

    /**
     * Runs this Job using the [executor].
     */
    fun start(executor: Executor) {
        executor.execute(this)
    }

    /**
     * Stops immediately this Job.
     */
    fun stop() {
        cancellationToken.cancel()
        task.close()
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
