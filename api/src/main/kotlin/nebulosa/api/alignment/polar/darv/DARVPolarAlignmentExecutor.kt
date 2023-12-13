package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.services.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.nio.file.Path

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVPolarAlignmentExecutor(
    private val jobLauncher: JobLauncher,
    private val messageService: MessageService,
    private val capturesPath: Path,
) : Consumer<DARVPolarAlignmentEvent> {

    private val jobExecutions = HashMap<Pair<Camera, GuideOutput>, JobExecution>(1)

    @Synchronized
    fun execute(request: DARVStart) {
        val camera = requireNotNull(request.camera)
        val guideOutput = requireNotNull(request.guideOutput)

        if (isRunning(camera, guideOutput)) {
            throw IllegalStateException("DARV Polar Alignment job is already running")
        }

        LOG.info("starting DARV polar alignment. data={}", request)

        val cameraRequest = CameraStartCaptureRequest(
            camera = camera,
            exposureTime = request.exposureTime + request.initialPause,
            savePath = Path.of("$capturesPath", "${camera.name}-DARV.fits")
        )

        val darvJob = DARVPolarAlignmentJob(request, cameraRequest)
        val jobExecution = jobLauncher.launch(darvJob)
        jobExecutions[camera to guideOutput] = jobExecution
    }

    @Synchronized
    fun stop(camera: Camera, guideOutput: GuideOutput) {
        val jobExecution = jobExecutions[camera to guideOutput] ?: return
        jobExecution.stop()
    }

    fun isRunning(camera: Camera, guideOutput: GuideOutput): Boolean {
        val jobExecution = jobExecutions[camera to guideOutput] ?: return false
        return !jobExecution.isDone
    }

    override fun accept(event: DARVPolarAlignmentEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVPolarAlignmentExecutor>()
    }
}
