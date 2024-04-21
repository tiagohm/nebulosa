package nebulosa.api.guiding

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.time.Stopwatch
import nebulosa.guiding.Guider
import nebulosa.log.loggerFor

data class WaitForSettleTask(
    @JvmField val guider: Guider?,
) : Task<WaitForSettleEvent>() {

    private val stopwatch = Stopwatch()

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.isSettling && !cancellationToken.isDone) {
            LOG.info("waiting for guiding to settle")

            onNext(WaitForSettleEvent.Started(this))

            stopwatch.start()
            guider.waitForSettle(cancellationToken)
            stopwatch.stop()

            onNext(WaitForSettleEvent.Finished(this, stopwatch.elapsed))
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WaitForSettleTask>()
    }
}
