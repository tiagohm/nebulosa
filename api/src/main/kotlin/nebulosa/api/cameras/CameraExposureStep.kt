package nebulosa.api.cameras

import nebulosa.api.guiding.WaitForSettleListener
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.debug
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

data class CameraExposureStep(override val request: CameraStartCaptureRequest) : CameraStartCaptureStep, DelayStepListener, WaitForSettleListener {

    @JvmField val camera = requireNotNull(request.camera)

    @JvmField val exposureTime = request.exposureTime
    @JvmField val exposureAmount = request.exposureAmount
    @JvmField val exposureDelay = request.exposureDelay

    @JvmField val estimatedCaptureTime: Duration = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(exposureTime.toNanos() * exposureAmount + exposureDelay.toNanos() * (exposureAmount - 1))

    private val latch = CountUpDownLatch()
    private val listeners = LinkedHashSet<CameraCaptureListener>()

    @Volatile private var aborted = false
    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = Duration.ZERO!!

    private var stepExecution: StepExecution? = null

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
                    stepExecution?.onCameraExposureElapsed(exposureElapsedTime, exposureRemainingTime, exposureProgress)
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        camera.enableBlob()

        captureElapsedTime = Duration.ZERO

        jobExecution.context[CAPTURE_ELAPSED_TIME] = Duration.ZERO
        jobExecution.context[CAPTURE_PROGRESS] = 0.0
        jobExecution.context[CAPTURE_REMAINING_TIME] = exposureTime
        jobExecution.context[EXPOSURE_ELAPSED_TIME] = Duration.ZERO
        jobExecution.context[EXPOSURE_REMAINING_TIME] = estimatedCaptureTime
        jobExecution.context[EXPOSURE_PROGRESS] = 0.0

        listeners.forEach { it.onCaptureStarted(this, jobExecution) }
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        listeners.forEach { it.onCaptureFinished(this, jobExecution) }
        listeners.clear()
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        this.stepExecution = stepExecution
        EventBus.getDefault().register(this)
        executeCapture(stepExecution)
        EventBus.getDefault().unregister(this)
        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        LOG.info("stopping camera exposure. camera={}", camera)
        camera.abortCapture()
        camera.disableBlob()
        aborted = true
        latch.reset()
    }

    override fun onDelayElapsed(step: DelayStep, stepExecution: StepExecution) {
        val waitTime = stepExecution.context.getDuration(DelayStep.WAIT_TIME)
        captureElapsedTime += waitTime
        stepExecution.onCameraExposureElapsed(Duration.ZERO, Duration.ZERO, 1.0)
    }

    override fun onSettleStarted(step: WaitForSettleStep, stepExecution: StepExecution) {
        stepExecution.onCameraExposureElapsed(Duration.ZERO, Duration.ZERO, 1.0)
    }

    override fun onSettleFinished(step: WaitForSettleStep, stepExecution: StepExecution) {
        stepExecution.onCameraExposureElapsed(Duration.ZERO, Duration.ZERO, 1.0)
    }

    private fun executeCapture(stepExecution: StepExecution) {
        if (camera.connected && !aborted) {
            synchronized(camera) {
                LOG.debug { "camera exposure started. request=%s".format(request) }

                latch.countUp()

                stepExecution.context[EXPOSURE_COUNT] = ++exposureCount

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

                LOG.debug { "camera exposure finished. aborted=%s, camera=%s".format(aborted, camera) }
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
            LOG.info("saving FITS. path={}", savePath)

            savePath.createParentDirectories()
            stream.transferAndClose(savePath.outputStream())

            stepExecution!!.context[SAVE_PATH] = savePath

            listeners.forEach { it.onExposureFinished(this, stepExecution!!) }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            aborted = true
        } finally {
            latch.countDown()
        }
    }

    private fun StepExecution.onCameraExposureElapsed(elapsedTime: Duration, remainingTime: Duration, progress: Double) {
        val captureElapsedTime = captureElapsedTime + elapsedTime
        var captureRemainingTime = Duration.ZERO
        var captureProgress = 0.0

        if (!request.isLoop) {
            captureRemainingTime = if (estimatedCaptureTime > captureElapsedTime) estimatedCaptureTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedCaptureTime - captureRemainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
        }

        context[EXPOSURE_ELAPSED_TIME] = elapsedTime
        context[EXPOSURE_REMAINING_TIME] = remainingTime
        context[EXPOSURE_PROGRESS] = progress
        context[CAPTURE_ELAPSED_TIME] = captureElapsedTime
        context[CAPTURE_REMAINING_TIME] = captureRemainingTime
        context[CAPTURE_PROGRESS] = captureProgress

        listeners.forEach { it.onExposureElapsed(this@CameraExposureStep, this) }
    }

    companion object {

        const val EXPOSURE_COUNT = "CAMERA_EXPOSURE.EXPOSURE_COUNT"
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
