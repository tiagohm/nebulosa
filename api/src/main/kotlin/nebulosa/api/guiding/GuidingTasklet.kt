package nebulosa.api.guiding

import nebulosa.api.cameras.CameraStartCapture
import nebulosa.api.cameras.CameraExposureTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayTasklet
import nebulosa.guiding.Guider
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nom.tam.fits.Header
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import java.nio.file.Path
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureTimeMillis
import kotlin.time.Duration

data class GuidingTasklet(
    private val camera: Camera,
    private val guider: Guider,
    private val startLooping: GuideStartLooping,
) : StoppableTasklet, JobExecutionListener {

    private val startCapture = CameraStartCapture(savePath = Path.of("@guiding"), saveInMemory = true)

    private val cameraExposureTasklet = CameraExposureTasklet(startCapture)
    private val delayTasklet = DelayTasklet(Duration.ZERO)
    private val guideImage = LinkedBlockingQueue<Image>()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        cameraExposureTasklet.execute(contribution, chunkContext)

        val image = guideImage.take()

        return if (image === DUMMY_IMAGE) {
            RepeatStatus.FINISHED
        } else {
            val elapsedTime = measureTimeMillis { guider.processImage(image) }
            val waitTime = startCapture.exposureDelayInSeconds * 1000L - elapsedTime // TODO: FIX ME

            if (waitTime in 100L..60000L) {
                contribution.stepExecution.executionContext.putLong(DelayTasklet.DELAY_TIME_NAME, waitTime)
                delayTasklet.execute(contribution, chunkContext)
            }

            RepeatStatus.CONTINUABLE
        }
    }

    override fun stop() {
        guider.stopGuiding()
        guideImage.offer(DUMMY_IMAGE)
        cameraExposureTasklet.stop()
    }

    override fun beforeJob(jobExecution: JobExecution) {
        cameraExposureTasklet.beforeJob(jobExecution)
    }

    override fun afterJob(jobExecution: JobExecution) {
        cameraExposureTasklet.afterJob(jobExecution)
    }

//    override fun onCameraExposureFinished(camera: Camera, image: Image?, path: Path?) {
//        if (image != null) {
//            guideImage.offer(image)
//            listener?.onCameraExposureFinished(camera, image, path)
//        }
//    }

    companion object {

        @JvmStatic private val DUMMY_IMAGE = Image(1, 1, Header(), true)
    }
}
