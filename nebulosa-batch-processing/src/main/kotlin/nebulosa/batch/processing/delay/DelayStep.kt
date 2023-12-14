package nebulosa.batch.processing.delay

import nebulosa.batch.processing.*
import java.time.Duration

data class DelayStep(@JvmField val duration: Duration) : Step, JobExecutionListener {

    private val listeners = HashSet<DelayStepListener>()

    @Volatile private var aborted = false

    fun registerDelayStepListener(listener: DelayStepListener) {
        listeners.add(listener)
    }

    fun unregisterDelayStepListener(listener: DelayStepListener) {
        listeners.remove(listener)
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        var remainingTime = duration

        if (!aborted && remainingTime > Duration.ZERO) {
            while (!aborted && remainingTime > Duration.ZERO) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > Duration.ZERO) {
                    stepExecution.context[REMAINING_TIME] = remainingTime
                    stepExecution.context[WAIT_TIME] = waitTime

                    val progress = (duration.toNanos() - remainingTime.toNanos()) / duration.toNanos().toDouble()
                    stepExecution.context[PROGRESS] = progress

                    listeners.forEach { it.onDelayElapsed(this, stepExecution) }
                    Thread.sleep(waitTime.toMillis())
                    remainingTime -= waitTime
                }
            }

            stepExecution.context[REMAINING_TIME] = Duration.ZERO
            stepExecution.context[WAIT_TIME] = Duration.ZERO

            listeners.forEach { it.onDelayElapsed(this, stepExecution) }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        aborted = true
    }

    override fun afterJob(jobExecution: JobExecution) {
        listeners.clear()
    }

    companion object {

        const val REMAINING_TIME = "DELAY.REMAINING_TIME"
        const val WAIT_TIME = "DELAY.WAIT_TIME"
        const val PROGRESS = "DELAY.PROGRESS"

        @JvmField val DELAY_INTERVAL = Duration.ofMillis(500)!!
    }
}
