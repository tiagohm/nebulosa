package nebulosa.api.guiding

import nebulosa.batch.processing.*
import nebulosa.guiding.Guider

data class WaitForSettleStep(@JvmField val guider: Guider) : Step, JobExecutionListener {

    private val listeners = LinkedHashSet<WaitForSettleListener>()

    fun registerWaitForSettleListener(listener: WaitForSettleListener) {
        listeners.add(listener)
    }

    fun unregisterWaitForSettleListener(listener: WaitForSettleListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (guider.isSettling && !stepExecution.jobExecution.cancellationToken.isDone) {
            listeners.forEach { it.onSettleStarted(this, stepExecution) }
            guider.waitForSettle(stepExecution.jobExecution.cancellationToken)
            listeners.forEach { it.onSettleFinished(this, stepExecution) }
        }

        return StepResult.FINISHED
    }

    override fun afterJob(jobExecution: JobExecution) {
        listeners.clear()
    }
}
