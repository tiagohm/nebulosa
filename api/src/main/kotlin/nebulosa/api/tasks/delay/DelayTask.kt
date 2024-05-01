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

        val event = DelayEvent(this)

        if (!cancellationToken.isDone && remainingTime > 0L) {
            LOG.info("delaying for {} ms", remainingTime)

            while (!cancellationToken.isDone && remainingTime > 0L) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0L) {
                    val progress = (durationTime - remainingTime) / durationTime.toDouble()
                    onNext(event.copy(remainingTime = Duration.ofMillis(remainingTime), waitTime = Duration.ofMillis(waitTime), progress = progress))

                    Thread.sleep(waitTime)

                    remainingTime -= waitTime
                }
            }

            onNext(event.copy(progress = 1.0))
        }
    }

    companion object {

        const val DELAY_INTERVAL = 500L
        @JvmStatic private val LOG = loggerFor<DelayTask>()
    }
}
