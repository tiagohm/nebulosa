package nebulosa.batch.processing

import nebulosa.common.concurrency.CancellationToken
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class JobExecution(
    val job: Job,
    val context: ExecutionContext,
    val jobLauncher: JobLauncher,
    val stepInterceptors: List<StepInterceptor>,
    val startedAt: LocalDateTime = LocalDateTime.now(),
) {

    var status = JobStatus.STARTING
        internal set

    var finishedAt: LocalDateTime? = null
        internal set

    @JvmField internal val completable = CompletableFuture<Boolean>()
    @JvmField val cancellationToken = CancellationToken()

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JobExecution) return false

        if (job != other.job) return false
        if (context != other.context) return false
        if (jobLauncher != other.jobLauncher) return false
        if (stepInterceptors != other.stepInterceptors) return false
        if (startedAt != other.startedAt) return false
        if (status != other.status) return false
        if (finishedAt != other.finishedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = job.hashCode()
        result = 31 * result + context.hashCode()
        result = 31 * result + jobLauncher.hashCode()
        result = 31 * result + stepInterceptors.hashCode()
        result = 31 * result + startedAt.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (finishedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString() = "JobExecution(job=$job, context=$context, startedAt=$startedAt," +
            " status=$status, finishedAt=$finishedAt)"
}
