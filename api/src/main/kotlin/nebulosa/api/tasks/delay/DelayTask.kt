package nebulosa.api.tasks.delay

import nebulosa.api.tasks.AbstractTask
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationToken
import java.time.Duration

data class DelayTask(
    @JvmField val duration: Duration,
) : AbstractTask<DelayEvent>() {

    @Volatile private var remainingTime = Duration.ZERO
    @Volatile private var waitTime = Duration.ZERO
    @Volatile private var progress = 0.0

    override fun execute(cancellationToken: CancellationToken) {
        val durationTime = duration.toMillis()
        var remainingTime = durationTime

        if (!cancellationToken.isCancelled && remainingTime > 0L) {
            LOG.info("Delay started. duration={}", remainingTime)

            while (!cancellationToken.isCancelled && remainingTime > 0L) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0L) {
                    progress = (durationTime - remainingTime) / durationTime.toDouble()
                    this.remainingTime = Duration.ofMillis(remainingTime)
                    this.waitTime = Duration.ofMillis(waitTime)
                    sendEvent()

                    Thread.sleep(waitTime)

                    remainingTime -= waitTime
                }
            }

            this.remainingTime = Duration.ZERO
            this.waitTime = Duration.ZERO
            progress = 1.0

            sendEvent()
        }
    }

    override fun reset() {
        remainingTime = Duration.ZERO
        waitTime = Duration.ZERO
        progress = 0.0
    }

    private fun sendEvent() {
        onNext(DelayEvent(this, remainingTime, waitTime, progress))
    }

    companion object {

        const val DELAY_INTERVAL = 500L
        @JvmStatic private val LOG = loggerFor<DelayTask>()
    }
}
