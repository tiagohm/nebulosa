package nebulosa.api.guiding

import nebulosa.api.tasks.Task
import nebulosa.guiding.Guider
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationToken

data class WaitForSettleTask(
    @JvmField val guider: Guider?,
) : Task {

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.isSettling && !cancellationToken.isCancelled) {
            LOG.info("Wait For Settle started")
            guider.waitForSettle(cancellationToken)
            LOG.info("Wait For Settle finished")
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WaitForSettleTask>()
    }
}
