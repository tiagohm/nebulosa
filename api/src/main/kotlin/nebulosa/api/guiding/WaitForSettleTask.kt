package nebulosa.api.guiding

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.log.loggerFor
import java.time.Duration
import kotlin.system.measureTimeMillis

data class WaitForSettleTask(
    @JvmField val guider: Guider?,
) : Task<WaitForSettleEvent>() {

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.isSettling && !cancellationToken.isDone) {
            LOG.info("waiting for guiding to settle")

            onNext(WaitForSettleEvent(this))
            val elapsedTime = measureTimeMillis { guider.waitForSettle(cancellationToken) }
            onNext(WaitForSettleEvent(this, Duration.ofMillis(elapsedTime)))
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WaitForSettleTask>()
    }
}
