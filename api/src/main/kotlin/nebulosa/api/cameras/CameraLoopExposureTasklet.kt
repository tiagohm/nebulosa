package nebulosa.api.cameras

import nebulosa.api.sequencer.PublishSequenceTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus

data class CameraLoopExposureTasklet(override val request: CameraStartCaptureRequest) :
    PublishSequenceTasklet<CameraCaptureEvent>(), CameraStartCaptureTasklet, JobExecutionListener {

    private val exposureTasklet = CameraExposureTasklet(request)
    private val delayTasklet = DelayTasklet(request.exposureDelay)

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
