package nebulosa.job.manager.delay

import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.time.Duration

data class DelayTask(
    @JvmField val job: Job,
    @JvmField val duration: Long,
) : Task {

    constructor(job: Job, duration: Duration) : this(job, duration.toMillis())

    override fun run() {
        var remainingTime = duration

        if (!job.isCancelled && remainingTime > 0L) {
            LOG.debug { "Delay started. duration=$duration ms" }

            job.accept(DelayStarted(job, this))

            while (!job.isCancelled && remainingTime > 0L) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0L) {
                    val progress = (duration - remainingTime) / duration.toDouble()
                    job.accept(DelayElapsed(job, this, remainingTime * 1000L, (duration - remainingTime) * 1000L, waitTime * 1000L, progress))

                    Thread.sleep(waitTime)

                    remainingTime -= waitTime
                }
            }

            job.accept(DelayFinished(job, this))

            LOG.debug { "Delay finished. duration=$duration ms" }
        }
    }

    companion object {

        const val DELAY_INTERVAL = 500L

        @JvmStatic private val LOG = loggerFor<DelayTask>()
    }
}
