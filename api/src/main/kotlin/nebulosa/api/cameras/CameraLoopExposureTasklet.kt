package nebulosa.api.cameras

import nebulosa.api.sequencer.AbstractSequenceTasklet
import nebulosa.api.tasklets.delay.DelayTasklet
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import kotlin.time.Duration.Companion.seconds

data class CameraLoopExposureTasklet(private val request: CameraCaptureRequest) :
    AbstractSequenceTasklet<CameraCaptureEvent>(), JobExecutionListener {

    private val exposureTasklet = CameraExposureTasklet(request)
    private val delayTasklet = DelayTasklet(request.exposureDelayInSeconds.seconds)

    init {
        exposureTasklet.subscribe(this)
        delayTasklet.subscribe(exposureTasklet)
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        exposureTasklet.execute(contribution, chunkContext)
        delayTasklet.execute(contribution, chunkContext)
        return RepeatStatus.CONTINUABLE
    }

    override fun stop() {
        exposureTasklet.stop()
        delayTasklet.stop()
    }

    override fun beforeJob(jobExecution: JobExecution) {
        exposureTasklet.beforeJob(jobExecution)
        delayTasklet.beforeJob(jobExecution)
    }

    override fun afterJob(jobExecution: JobExecution) {
        exposureTasklet.afterJob(jobExecution)
        delayTasklet.afterJob(jobExecution)
    }
}
