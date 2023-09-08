package nebulosa.api.guiding

import nebulosa.api.cameras.*
import nebulosa.guiding.internal.InternalGuider
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import nom.tam.fits.Header
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import java.nio.file.Path
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureTimeMillis

data class GuidingTasklet(
    private val camera: Camera,
    private val guider: InternalGuider,
    private val startLooping: GuideStartLoopingRequest,
    private val listener: CameraCaptureEventListener,
) : StoppableTasklet, CameraCaptureEventListener, JobExecutionListener {

    private val startCapture = CameraStartCaptureRequest()

    init {
        startCapture.savePath = Path.of("@guiding")
    }

    private val cameraExposureTasklet = CameraExposureTasklet(camera, startCapture, this, true)
    private val guideImage = LinkedBlockingQueue<Image>()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        cameraExposureTasklet.execute(contribution, chunkContext)

        val image = guideImage.take()

        return if (image === DUMMY_IMAGE) {
            RepeatStatus.FINISHED
        } else {
            val elapsedTime = measureTimeMillis { guider.processImage(image) }
            val waitTime = startCapture.exposureDelayInSeconds * 1000L - elapsedTime

            if (waitTime in 100L..60000L) {
                LOG.info("waiting {} ms before starting next capture", waitTime)
                Thread.sleep(waitTime)
            }

            RepeatStatus.CONTINUABLE
        }
    }

    override fun stop() {
        guider.stopGuiding()
        guideImage.offer(DUMMY_IMAGE)
        cameraExposureTasklet.stop()
    }

    override fun onCameraCaptureEvent(event: CameraCaptureEvent) {
        if (event is CameraExposureSaved) {
            guideImage.offer(event.image!!)
            listener.onCameraCaptureEvent(event)
        }
    }

    companion object {

        @JvmStatic private val DUMMY_IMAGE = Image(1, 1, Header(), true)
        @JvmStatic private val LOG = loggerFor<GuidingTasklet>()
    }
}
