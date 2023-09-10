package nebulosa.api.cameras

import nebulosa.api.tasklets.delay.DelayTasklet
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import java.nio.file.Path
import kotlin.time.Duration

data class CameraLoopExposureTasklet(
    private val camera: Camera,
    private val exposureTime: Duration = Duration.ZERO,
    private val exposureDelay: Duration = Duration.ZERO,
    private val x: Int = camera.minX, private val y: Int = camera.minY,
    private val width: Int = camera.maxWidth, private val height: Int = camera.maxHeight,
    private val frameFormat: String? = null,
    private val frameType: FrameType = FrameType.LIGHT,
    private val binX: Int = camera.binX, private val binY: Int = binX,
    private val gain: Int = camera.gain, private val offset: Int = camera.offset,
    private val autoSave: Boolean = false,
    private val savePath: Path? = null,
    private val listener: CameraCaptureListener? = null,
) : StoppableTasklet, JobExecutionListener {

    private val exposureTasklet = CameraExposureTasklet(
        camera,
        exposureTime, 0, exposureDelay,
        x, y, width, height,
        frameFormat, frameType,
        binX, binY, gain, offset,
        autoSave, savePath, listener,
    )

    private val delayTasklet = DelayTasklet(exposureDelay, exposureTasklet)

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
