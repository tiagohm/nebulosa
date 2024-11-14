package nebulosa.job.manager.delay

import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.d
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
            LOG.d { debug("Delay started. duration={} ms", duration) }

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

            LOG.d { debug("Delay finished. duration={} ms", duration) }
        }
    }

    companion object {

        const val DELAY_INTERVAL = 500L

        private val LOG = loggerFor<DelayTask>()
    }
}
