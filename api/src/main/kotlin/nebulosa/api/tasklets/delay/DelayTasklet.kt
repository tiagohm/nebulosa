package nebulosa.api.tasklets.delay

import nebulosa.api.sequencer.AbstractSequenceTasklet
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class DelayTasklet(private val duration: Duration) : AbstractSequenceTasklet<DelayElapsed>(), JobExecutionListener {

    private val aborted = AtomicBoolean()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val delayTimeInMilliseconds = contribution.stepExecution.executionContext
            .getLong(DELAY_TIME_NAME, duration.inWholeMilliseconds)
        val delayTime = delayTimeInMilliseconds.milliseconds

        var remainingTime = delayTimeInMilliseconds

        if (remainingTime > 0L) {
            while (!aborted.get() && remainingTime > 0L) {
                val waitTime = min(remainingTime, DELAY_INTERVAL)

                if (waitTime > 0) {
                    onNext(DelayElapsed(remainingTime.milliseconds, delayTime, waitTime.milliseconds))
                    Thread.sleep(waitTime)
                    remainingTime -= waitTime
                }
            }

            onNext(DelayElapsed(Duration.ZERO, delayTime, Duration.ZERO))
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

        const val DELAY_INTERVAL = 500L
        const val DELAY_TIME_NAME = "delayTime"
    }
}
