package nebulosa.api.tasks.delay

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.log.loggerFor
import java.time.Duration

data class DelayTask(
    @JvmField val duration: Duration,
) : Task<DelayEvent>() {

    override fun execute(cancellationToken: CancellationToken) {
        val durationTime = duration.toMillis()
        var remainingTime = durationTime

        if (!cancellationToken.isDone && remainingTime > 0L) {
            LOG.info("delaying for {} ms", remainingTime)

            while (!cancellationToken.isDone && remainingTime > 0L) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0L) {
                    val progress = (durationTime - remainingTime) / durationTime.toDouble()
                    val event = DelayEvent.Elapsed(this, Duration.ofMillis(remainingTime), Duration.ofMillis(waitTime), progress)
                    onNext(event)

                    Thread.sleep(waitTime)

                    remainingTime -= waitTime
                }
            }

            onNext(DelayEvent.Elapsed(this, Duration.ZERO, Duration.ZERO, 1.0))
        }
    }

    companion object {

        const val DELAY_INTERVAL = 500L
        @JvmStatic private val LOG = loggerFor<DelayTask>()
    }
}
