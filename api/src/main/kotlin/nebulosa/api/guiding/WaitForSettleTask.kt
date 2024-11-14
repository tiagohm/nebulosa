package nebulosa.api.guiding

import nebulosa.guiding.Guider
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.loggerFor

data class WaitForSettleTask(
    @JvmField val job: Job,
    @JvmField val guider: Guider?,
) : Task {

    override fun run() {
        if (guider != null && guider.isSettling && !job.isCancelled) {
            LOG.debug("Wait For Settle started")
            job.accept(WaitForSettleStarted(job, this))
            guider.waitForSettle()
            job.accept(WaitForSettleFinished(job, this))
            LOG.debug("Wait For Settle finished")
        }
    }

    companion object {

        private val LOG = loggerFor<WaitForSettleTask>()
    }
}
