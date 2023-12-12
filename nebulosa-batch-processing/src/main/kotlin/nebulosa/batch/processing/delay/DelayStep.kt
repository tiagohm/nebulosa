package nebulosa.batch.processing.delay

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import java.time.Duration

data class DelayStep(@JvmField val duration: Duration) : Step {

    @Volatile private var aborted = false
    private val listeners = HashSet<DelayListener>()

    fun registerListener(listener: DelayListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: DelayListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        var remainingTime = duration

        if (remainingTime > Duration.ZERO) {
            while (!aborted && remainingTime > Duration.ZERO) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > Duration.ZERO) {
                    stepExecution.jobExecution.context[REMAINING_TIME] = remainingTime
                    stepExecution.jobExecution.context[WAIT_TIME] = waitTime

                    val progress = (duration.toNanos() - remainingTime.toNanos()) / duration.toNanos().toDouble()
                    stepExecution.jobExecution.context[PROGRESS] = progress

                    listeners.forEach { it.onDelayElapsed(stepExecution) }
                    Thread.sleep(waitTime.toMillis())
                    remainingTime -= waitTime
                }
            }

            stepExecution.jobExecution.context[REMAINING_TIME] = Duration.ZERO
            stepExecution.jobExecution.context[WAIT_TIME] = Duration.ZERO

            listeners.forEach { it.onDelayElapsed(stepExecution) }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        aborted = true
    }

    companion object {

        const val REMAINING_TIME = "DELAY.REMAINING_TIME"
        const val WAIT_TIME = "DELAY.WAIT_TIME"
        const val PROGRESS = "DELAY.PROGRESS"

        @JvmField val DELAY_INTERVAL = Duration.ofMillis(500)!!
    }
}
