package nebulosa.batch.processing

import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

data class JobExecution(
    val job: Job,
    val context: ExecutionContext,
    val jobLauncher: JobLauncher,
    val stepInterceptors: List<StepInterceptor>,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    var status: JobStatus = JobStatus.STARTING,
    var finishedAt: LocalDateTime? = null,
) : Stoppable {

    private val completable = CompletableFuture<Boolean>()

    inline val jobId
        get() = job.id

    inline val canContinue
        get() = status == JobStatus.STARTED

    inline val isStopping
        get() = status == JobStatus.STOPPING

    inline val isStopped
        get() = status == JobStatus.STOPPED

    inline val isCompleted
        get() = status == JobStatus.COMPLETED

    inline val isFailed
        get() = status == JobStatus.FAILED

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
            status = JobStatus.STOPPING
            job.stop(mayInterruptIfRunning)
        }
    }
}
