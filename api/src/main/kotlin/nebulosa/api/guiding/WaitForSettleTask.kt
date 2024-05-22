package nebulosa.api.guiding

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.log.loggerFor

data class WaitForSettleTask(
    @JvmField val guider: Guider?,
) : Task {

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.isSettling && !cancellationToken.isDone) {
            LOG.info("Wait For Settle started")
            guider.waitForSettle(cancellationToken)
            LOG.info("Wait For Settle finished")
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WaitForSettleTask>()
    }
}
