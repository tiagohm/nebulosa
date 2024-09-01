package nebulosa.job.manager.delay

import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.debug
import nebulosa.log.loggerFor

data class DelayTask(@JvmField val durationInMilliseconds: Long) : Task {

    override fun execute(job: Job) {
        var remainingTime = durationInMilliseconds

        if (!job.isCancelled && remainingTime > 0L) {
            LOG.debug { "Delay started. duration=$durationInMilliseconds ms" }

            job.accept(DelayStarted(job, this))

            while (!job.isCancelled && remainingTime > 0L) {
                job.waitForPause()

                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0L) {
                    val progress = (durationInMilliseconds - remainingTime) / durationInMilliseconds.toDouble()
                    job.accept(DelayElapsed(job, this, remainingTime, durationInMilliseconds - remainingTime, waitTime, progress))

                    Thread.sleep(waitTime)

                    remainingTime -= waitTime
                }
            }

            job.accept(DelayFinished(job, this))

            LOG.debug { "Delay finished. duration=$durationInMilliseconds ms" }
        }
    }

    companion object {

        const val DELAY_INTERVAL = 500L

        @JvmStatic private val LOG = loggerFor<DelayTask>()
    }
}
