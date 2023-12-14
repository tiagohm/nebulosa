package nebulosa.api.cameras

import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

data class CameraExposureStep(override val request: CameraStartCaptureRequest) : CameraStartCaptureStep, DelayStepListener {

    @JvmField val camera = requireNotNull(request.camera)

    private val latch = CountUpDownLatch()
    private val listeners = HashSet<CameraCaptureListener>()

    @Volatile private var aborted = false
    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = Duration.ZERO!!

    private val exposureTime = request.exposureTime
    private val exposureDelay = request.exposureDelay

    private val estimatedTime = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(exposureTime.toNanos() * request.exposureAmount + exposureDelay.toNanos() * (request.exposureAmount - 1))

    private lateinit var stepExecution: StepExecution

    override fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return listeners.add(listener)
    }

    override fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean {
        return listeners.remove(listener)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        if (event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event.fits)
                    latch.countDown()
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                    aborted = true
                }
                is CameraExposureProgressChanged -> {
                    val exposureRemainingTime = event.device.exposureTime
                    val exposureElapsedTime = exposureTime - exposureRemainingTime
                    val exposureProgress = exposureElapsedTime.toNanos().toDouble() / exposureTime.toNanos()
                    onCameraExposureElapsed(exposureElapsedTime, exposureRemainingTime, exposureProgress)
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        camera.enableBlob()
        EventBus.getDefault().register(this)
        listeners.forEach { it.onCaptureStarted(this, jobExecution) }
        captureElapsedTime = Duration.ZERO
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        EventBus.getDefault().unregister(this)
        listeners.forEach { it.onCaptureFinished(this, jobExecution) }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        this.stepExecution = stepExecution
        LOG.info("starting exposure. camera=${camera.name}")
        executeCapture(stepExecution)
        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        LOG.info("stopping exposure. camera=${camera.name}")
        camera.abortCapture()
        camera.disableBlob()
        aborted = true
        latch.reset()
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val waitTime = stepExecution.context[DelayStep.WAIT_TIME] as Duration
        captureElapsedTime += waitTime
        onCameraExposureElapsed(Duration.ZERO, Duration.ZERO, 1.0)
    }

    private fun executeCapture(stepExecution: StepExecution) {
        if (camera.connected && !aborted) {
            synchronized(camera) {
                latch.countUp()

                stepExecution.context[EXPOSURE_AMOUNT] = ++exposureCount

                listeners.forEach { it.onExposureStarted(this, stepExecution) }

                if (request.width > 0 && request.height > 0) {
                    camera.frame(request.x, request.y, request.width, request.height)
                }

                camera.frameType(request.frameType)
                camera.frameFormat(request.frameFormat)
                camera.bin(request.binX, request.binY)
                camera.gain(request.gain)
                camera.offset(request.offset)
                camera.startCapture(exposureTime)

                latch.await()

                captureElapsedTime += exposureTime

                LOG.info("camera exposure finished")
            }
        }
    }

    private fun save(stream: InputStream) {
        val savePath = if (request.autoSave) {
            val now = LocalDateTime.now()
            val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), request.frameType)
            Path.of("${request.savePath}", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Path.of("${request.savePath}", fileName)
        }

        try {
            LOG.info("saving FITS at $savePath...")

            savePath.createParentDirectories()
            stream.transferAndClose(savePath.outputStream())

            stepExecution.context[SAVE_PATH] = savePath

            listeners.forEach { it.onExposureFinished(this, stepExecution) }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            aborted = true
        }
    }

    private fun onCameraExposureElapsed(elapsedTime: Duration, remainingTime: Duration, progress: Double) {
        val captureElapsedTime = captureElapsedTime + elapsedTime
        var captureRemainingTime = Duration.ZERO
        var captureProgress = 0.0

        if (!request.isLoop) {
            captureRemainingTime = if (estimatedTime > captureElapsedTime) estimatedTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedTime - captureRemainingTime).toNanos().toDouble() / estimatedTime.toNanos()
        }

        stepExecution.context[EXPOSURE_ELAPSED_TIME] = elapsedTime
        stepExecution.context[EXPOSURE_REMAINING_TIME] = remainingTime
        stepExecution.context[EXPOSURE_PROGRESS] = progress
        stepExecution.context[CAPTURE_ELAPSED_TIME] = captureElapsedTime
        stepExecution.context[CAPTURE_REMAINING_TIME] = captureRemainingTime
        stepExecution.context[CAPTURE_PROGRESS] = captureProgress

        listeners.forEach { it.onExposureElapsed(this, stepExecution) }
    }

    companion object {

        const val EXPOSURE_AMOUNT = "CAMERA_EXPOSURE.EXPOSURE_AMOUNT"
        const val SAVE_PATH = "CAMERA_EXPOSURE.SAVE_PATH"
        const val EXPOSURE_ELAPSED_TIME = "CAMERA_EXPOSURE.EXPOSURE_ELAPSED_TIME"
        const val EXPOSURE_REMAINING_TIME = "CAMERA_EXPOSURE.EXPOSURE_REMAINING_TIME"
        const val EXPOSURE_PROGRESS = "CAMERA_EXPOSURE.EXPOSURE_PROGRESS"
        const val CAPTURE_ELAPSED_TIME = "CAMERA_EXPOSURE.CAPTURE_ELAPSED_TIME"
        const val CAPTURE_REMAINING_TIME = "CAMERA_EXPOSURE.CAPTURE_REMAINING_TIME"
        const val CAPTURE_PROGRESS = "CAMERA_EXPOSURE.CAPTURE_PROGRESS"

        @JvmStatic private val LOG = loggerFor<CameraExposureStep>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
