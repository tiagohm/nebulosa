package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

data class JobExecution(
    val job: Job,
    val context: ExecutionContext,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    var status: BatchStatus = BatchStatus.STARTING,
    var finishedAt: LocalDateTime? = null,
) : Stoppable {

    private val completable = CompletableFuture<Boolean>()

    val canContinue
        get() = status == BatchStatus.STARTED

    val isStopping
        get() = status == BatchStatus.STOPPING

    val isStopped
        get() = status == BatchStatus.STOPPED

    val isCompleted
        get() = status == BatchStatus.COMPLETED

    val isFailed
        get() = status == BatchStatus.FAILED

    val isDone
        get() = isCompleted || isFailed || isStopped

    fun waitForCompletion(timeout: Long = 0L, unit: TimeUnit = TimeUnit.MILLISECONDS): Boolean {
        try {
            if (timeout <= 0L) completable.get()
            else return completable.get(timeout, unit)
            return true
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    internal fun complete() {
        completable.complete(true)
    }

    internal fun completeExceptionally(e: Throwable) {
        completable.completeExceptionally(e)
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        if (!isDone) {
            status = BatchStatus.STOPPING
            job.stop(mayInterruptIfRunning)
        }
    }
}
