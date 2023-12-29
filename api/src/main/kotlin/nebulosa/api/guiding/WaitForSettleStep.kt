package nebulosa.api.guiding

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.guiding.Guider

data class WaitForSettleStep(@JvmField val guider: Guider) : Step {

    private val listeners = LinkedHashSet<WaitForSettleListener>()

    fun registerWaitForSettleListener(listener: WaitForSettleListener) {
        listeners.add(listener)
    }

    fun unregisterWaitForSettleListener(listener: WaitForSettleListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (guider.isSettling && !stepExecution.jobExecution.cancellationToken.isDone) {
            stepExecution.context[WAITING] = true
            listeners.forEach { it.onSettleStarted(this, stepExecution) }
            guider.waitForSettle(stepExecution.jobExecution.cancellationToken)
            stepExecution.context[WAITING] = false
            listeners.forEach { it.onSettleFinished(this, stepExecution) }
        }

        return StepResult.FINISHED
    }

    override fun afterJob(jobExecution: JobExecution) {
        listeners.clear()
    }

    companion object {

        const val WAITING = "WAIT_FOR_SETTLE.WAITING"
    }
}
