package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus

data class CameraLoopExposureTasklet(
    private val camera: Camera,
    private val startCapture: CameraStartCaptureRequest,
) : StoppableTasklet, JobExecutionListener {

    private val exposureTasklet = CameraExposureTasklet(camera, startCapture)
    private val delayTasklet = CameraDelayTasklet(camera, startCapture.exposureDelayInSeconds)

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
    }

    override fun afterJob(jobExecution: JobExecution) {
        exposureTasklet.afterJob(jobExecution)
    }
}
