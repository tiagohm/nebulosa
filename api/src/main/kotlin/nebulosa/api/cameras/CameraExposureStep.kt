package nebulosa.api.cameras

import nebulosa.api.guiding.WaitForSettleListener
import nebulosa.api.guiding.WaitForSettleStep
import nebulosa.batch.processing.ExecutionContext
import nebulosa.batch.processing.ExecutionContext.Companion.getDuration
import nebulosa.batch.processing.ExecutionContext.Companion.getInt
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.batch.processing.delay.DelayStepListener
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.debug
import nebulosa.log.loggerFor
import okio.sink
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

data class CameraExposureStep(
    override val camera: Camera,
    override val request: CameraStartCaptureRequest,
    private val virtualLoop: Boolean = false,
) : CameraStartCaptureStep, DelayStepListener, WaitForSettleListener {

    @JvmField val exposureTime = request.exposureTime
    @JvmField val exposureAmount = request.exposureAmount
    @JvmField val exposureDelay = request.exposureDelay

    @JvmField @Volatile var estimatedCaptureTime: Duration = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(exposureTime.toNanos() * exposureAmount + exposureDelay.toNanos() * (exposureAmount - 1))

    private val latch = CountUpDownLatch()
    private val listeners = LinkedHashSet<CameraCaptureListener>()

    @Volatile private var aborted = false
    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = Duration.ZERO!!

    @Volatile private var stepExecution: StepExecution? = null

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
                    save(event)
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                    aborted = true
                }
                is CameraExposureProgressChanged -> {
                    // minOf fix possible bug on SVBony exposure time.
                    val exposureRemainingTime = minOf(event.device.exposureTime, exposureTime)
                    val exposureElapsedTime = exposureTime - exposureRemainingTime
                    val exposureProgress = exposureElapsedTime.toNanos().toDouble() / exposureTime.toNanos()
                    stepExecution?.onCameraExposureElapsed(exposureElapsedTime, exposureRemainingTime, exposureProgress)
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        exposureCount = jobExecution.context.getInt(EXPOSURE_COUNT, exposureCount)
        captureElapsedTime = jobExecution.context.getDuration(CAPTURE_ELAPSED_TIME, captureElapsedTime)
        jobExecution.context.populateExecutionContext(Duration.ZERO, estimatedCaptureTime, 0.0)
        listeners.forEach { it.onCaptureStarted(this, jobExecution) }
    }

    override fun afterJob(jobExecution: JobExecution) {
        // TODO: BUG: Está desativando para todas as cameras. Fiz alguma coisa errada ou isso é um bug?
        // camera.disableBlob()
        listeners.forEach { it.onCaptureFinished(this, jobExecution) }
        listeners.clear()
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (request.isLoop || estimatedCaptureTime > Duration.ZERO) {
            this.stepExecution = stepExecution
            EventBus.getDefault().register(this)
            executeCapture(stepExecution)
            EventBus.getDefault().unregister(this)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        LOG.info("stopping camera exposure. camera={}", camera)
        camera.abortCapture()
        // camera.disableBlob()
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
                LOG.debug { "camera exposure started. estimatedCaptureTime=$estimatedCaptureTime, request=$request, context=${stepExecution.context}" }

                latch.countUp()

                stepExecution.context[EXPOSURE_COUNT] = ++exposureCount

                camera.enableBlob()

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
                stepExecution.context[CAPTURE_ELAPSED_TIME] = captureElapsedTime

                LOG.debug { "camera exposure finished. aborted=$aborted, camera=$camera, context=${stepExecution.context}" }
            }
        } else {
            LOG.warn("camera not connected or aborted. aborted=$aborted, camera=$camera")
        }
    }

    private fun save(event: CameraFrameCaptured) {
        try {
            val savedPath = request.makeSavePath(camera)

            LOG.info("saving FITS image at {}", savedPath)

            savedPath.createParentDirectories()

            if (event.stream != null) {
                event.stream!!.transferAndClose(savedPath.outputStream())
            } else if (event.image != null) {
                savedPath.sink().use(event.image!!::write)
            } else {
                LOG.warn("invalid event. camera={}", event.device)
                return
            }

            listeners.forEach { it.onExposureFinished(this, stepExecution!!, event.image, savedPath) }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS image", e)
            aborted = true
        } finally {
            latch.countDown()
        }
    }

    private fun StepExecution.onCameraExposureElapsed(elapsedTime: Duration, remainingTime: Duration, progress: Double) {
        context.populateExecutionContext(elapsedTime, remainingTime, progress)
        listeners.forEach { it.onExposureElapsed(this@CameraExposureStep, this) }
    }

    private fun ExecutionContext.populateExecutionContext(elapsedTime: Duration, remainingTime: Duration, progress: Double) {
        val captureElapsedTime = captureElapsedTime + elapsedTime
        var captureRemainingTime = Duration.ZERO
        var captureProgress = 0.0

        if (!request.isLoop && !virtualLoop) {
            captureRemainingTime = if (estimatedCaptureTime > captureElapsedTime) estimatedCaptureTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedCaptureTime - captureRemainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
        }

        this[EXPOSURE_ELAPSED_TIME] = elapsedTime
        this[EXPOSURE_REMAINING_TIME] = remainingTime
        this[EXPOSURE_PROGRESS] = progress
        this[CAPTURE_ELAPSED_TIME] = captureElapsedTime
        this[CAPTURE_REMAINING_TIME] = captureRemainingTime
        this[CAPTURE_PROGRESS] = captureProgress
    }

    companion object {

        const val EXPOSURE_COUNT = "CAMERA_EXPOSURE.EXPOSURE_COUNT"
        const val EXPOSURE_ELAPSED_TIME = "CAMERA_EXPOSURE.EXPOSURE_ELAPSED_TIME"
        const val EXPOSURE_REMAINING_TIME = "CAMERA_EXPOSURE.EXPOSURE_REMAINING_TIME"
        const val EXPOSURE_PROGRESS = "CAMERA_EXPOSURE.EXPOSURE_PROGRESS"
        const val CAPTURE_ELAPSED_TIME = "CAMERA_EXPOSURE.CAPTURE_ELAPSED_TIME"
        const val CAPTURE_REMAINING_TIME = "CAMERA_EXPOSURE.CAPTURE_REMAINING_TIME"
        const val CAPTURE_PROGRESS = "CAMERA_EXPOSURE.CAPTURE_PROGRESS"

        @JvmStatic private val LOG = loggerFor<CameraExposureStep>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")

        @JvmStatic
        internal fun CameraStartCaptureRequest.makeSavePath(
            camera: Camera, autoSave: Boolean = this.autoSave,
        ): Path {
            return if (autoSave) {
                val now = LocalDateTime.now()
                val savePath = autoSubFolderMode.pathFor(savePath!!, now)
                val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), frameType)
                Path.of("$savePath", fileName)
            } else {
                val fileName = "%s.fits".format(camera.name)
                Path.of("$savePath", fileName)
            }
        }
    }
}
