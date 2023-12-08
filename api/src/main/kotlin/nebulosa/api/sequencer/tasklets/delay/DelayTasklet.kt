package nebulosa.api.sequencer.tasklets.delay

import nebulosa.api.sequencer.PublishSequenceTasklet
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

data class DelayTasklet(val duration: Duration) : PublishSequenceTasklet<DelayEvent>(), JobExecutionListener {

    private val aborted = AtomicBoolean()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val stepExecution = contribution.stepExecution
        var remainingTime = duration

        if (remainingTime > Duration.ZERO) {
            while (!aborted.get() && remainingTime > Duration.ZERO) {
                val waitTime = minOf(remainingTime, DELAY_INTERVAL)

                if (waitTime > Duration.ZERO) {
                    onNext(DelayElapsed(remainingTime, waitTime, stepExecution, this))
                    Thread.sleep(waitTime.toMillis())
                    remainingTime -= waitTime
                }
            }

            onNext(DelayElapsed(Duration.ZERO, Duration.ZERO, stepExecution, this))
        }

        return RepeatStatus.FINISHED
    }

    override fun afterJob(jobExecution: JobExecution) {
        close()
    }

    override fun stop() {
        aborted.set(true)
    }

    fun wasAborted(): Boolean {
        return aborted.get()
    }

    companion object {

        @JvmField val DELAY_INTERVAL = Duration.ofMillis(500)!!
    }
}
