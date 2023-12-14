package nebulosa.api.cameras

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.batch.processing.StepExecution
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

@Component
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val asyncJobLauncher: JobLauncher,
) : CameraCaptureListener {

    private val jobExecutions = HashMap<Camera, JobExecution>(4)

    @Synchronized
    fun execute(request: CameraStartCaptureRequest) {
        val camera = requireNotNull(request.camera)

        check(!isCapturing(camera)) { "job is already running for camera: [${camera.name}]" }
        check(camera.connected) { "camera is not connected" }

        LOG.info("starting camera capture. data={}", request)

        val cameraCaptureJob = CameraCaptureJob(request, guider)
        cameraCaptureJob.registerListener(this)
        val jobExecution = asyncJobLauncher.launch(cameraCaptureJob)
        jobExecutions[camera] = jobExecution
    }

    fun stop(camera: Camera) {
        val jobExecution = jobExecutions[camera] ?: return
        jobExecution.stop()
    }

    fun isCapturing(camera: Camera): Boolean {
        val jobExecution = jobExecutions[camera] ?: return false
        return !jobExecution.isDone
    }

    override fun onCaptureStarted(step: CameraExposureStep, jobExecution: JobExecution) {
        // TODO: messageService.sendMessage(CameraCaptureStarted(step.request.camera!!, jobExecution))
    }

    override fun onExposureStarted(step: CameraExposureStep, stepExecution: StepExecution) {
        // TODO: messageService.sendMessage(CameraExposureStarted(step.request.camera!!, stepExecution))
    }

    override fun onExposureElapsed(step: CameraExposureStep, stepExecution: StepExecution) {
        // TODO: messageService.sendMessage(CameraExposureElapsed(step.request.camera!!, stepExecution))
    }

    override fun onExposureFinished(step: CameraExposureStep, stepExecution: StepExecution) {
        // TODO: messageService.sendMessage(CameraExposureFinished(step.request.camera!!, stepExecution))
    }

    override fun onCaptureFinished(step: CameraExposureStep, jobExecution: JobExecution) {
        // TODO: messageService.sendMessage(CameraCaptureFinished(step.request.camera!!, jobExecution))
    }

    // TODO: CameraCaptureIsWaiting

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureExecutor>()
    }
}
