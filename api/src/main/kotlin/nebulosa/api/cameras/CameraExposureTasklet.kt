package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_ELAPSED_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_IN_LOOP
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_IS_WAITING
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_AMOUNT
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_COUNT
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_TIME
import nebulosa.api.sequencer.AbstractSequenceTasklet
import nebulosa.api.tasklets.delay.DelayElapsed
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import java.io.InputStream
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

data class CameraExposureTasklet(
    private val camera: Camera,
    private val exposureTime: Duration = Duration.ZERO,
    private val exposureAmount: Int = 1, // 0 = looping
    private val exposureDelay: Duration = Duration.ZERO,
    private val x: Int = camera.minX, private val y: Int = camera.minY,
    private val width: Int = camera.maxWidth, private val height: Int = camera.maxHeight,
    private val frameFormat: String? = null,
    private val frameType: FrameType = FrameType.LIGHT,
    private val binX: Int = camera.binX, private val binY: Int = binX,
    private val gain: Int = camera.gain, private val offset: Int = camera.offset,
    private val autoSave: Boolean = false,
    private val savePath: Path? = null,
    private val saveInMemory: Boolean = false,
) : AbstractSequenceTasklet<CameraCaptureEvent>(), JobExecutionListener, Consumer<DelayElapsed> {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = 0L
    @Volatile private var exposureElapsedTime = 0L
    @Volatile private var stepExecution: StepExecution? = null

    private val isLoop = exposureAmount <= 0
    private val captureTime = if (isLoop) Duration.ZERO else exposureTime * exposureAmount + exposureDelay * (exposureAmount - 1)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        if (event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event.fits, stepExecution!!)
                    latch.countDown()
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                    aborted.set(true)
                }
                is CameraExposureProgressChanged -> {
                    val exposureRemainingTime = event.device.exposureTime
                    val exposureElapsedTime = exposureTime - exposureRemainingTime
                    this.exposureElapsedTime = exposureElapsedTime.inWholeMicroseconds

                    val exposureProgress = exposureElapsedTime / exposureTime
                    onCameraExposureUpdated(exposureRemainingTime, exposureProgress)
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        camera.enableBlob()
        EventBus.getDefault().register(this)
        jobExecution.executionContext.put(CAPTURE_IN_LOOP, isLoop)
        onNext(CameraCaptureStarted(camera, jobExecution))
        captureElapsedTime = 0L
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        EventBus.getDefault().unregister(this)
        onNext(CameraCaptureFinished(camera, jobExecution))
        close()
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        executeCapture(contribution)
        return RepeatStatus.FINISHED
    }

    override fun stop() {
        LOG.info("stopping exposure. camera=${camera.name}")
        camera.abortCapture()
        camera.disableBlob()
        aborted.set(true)
        latch.reset()
    }

    override fun accept(event: DelayElapsed) {
        captureElapsedTime += event.waitTime.inWholeMicroseconds
        val waitProgress = if (event.remainingTime > Duration.ZERO) 1.0 - event.delayTime / event.remainingTime else 1.0

        with(stepExecution!!.executionContext) {
            putDouble(WAIT_PROGRESS, waitProgress)
            putLong(WAIT_REMAINING_TIME, event.remainingTime.inWholeMicroseconds)
            putLong(WAIT_TIME, event.waitTime.inWholeMicroseconds)
            put(CAPTURE_IS_WAITING, true)
        }

        onNext(CameraExposureDelayElapsed(camera, stepExecution!!))
        onCameraExposureUpdated(Duration.ZERO, 1.0)
    }

    private fun executeCapture(contribution: StepContribution) {
        stepExecution = contribution.stepExecution

        if (camera.connected && !aborted.get()) {
            synchronized(camera) {
                latch.countUp()

                exposureCount++

                with(contribution.stepExecution.executionContext) {
                    putInt(EXPOSURE_COUNT, exposureCount)
                    putInt(EXPOSURE_AMOUNT, exposureAmount)
                    put(CAPTURE_IN_LOOP, isLoop)
                    put(CAPTURE_IS_WAITING, false)
                }

                onNext(CameraExposureStarted(camera, stepExecution!!))

                camera.frame(x, y, width, height)
                camera.frameType(frameType)
                if (!frameFormat.isNullOrEmpty()) camera.frameFormat(frameFormat)
                camera.bin(binX, binY)
                camera.gain(gain)
                camera.offset(offset)
                camera.startCapture(exposureTime)

                exposureElapsedTime = 0L

                LOG.info(
                    "starting camera exposure. camera={}, exposureTime={}, exposureAmount={}, exposureDelay={}, x={}, y={}, width={}, height={}," +
                            " frameFormat={}, frameType={}, binX={}, binY={}, gain={}, offset={}, autoSave={}, savePath={}, saveInMemory={}",
                    camera.name, exposureTime, exposureAmount,
                    exposureDelay, x, y, width, height, frameFormat, frameType, binX, binY,
                    gain, offset, autoSave, savePath, saveInMemory,
                )

                latch.await()

                exposureElapsedTime = 0L
                captureElapsedTime += exposureTime.inWholeMicroseconds

                LOG.info("camera exposure finished")
            }
        }
    }

    private fun save(inputStream: InputStream, stepExecution: StepExecution) {
        val savePath = if (saveInMemory) {
            savePath
        } else if (autoSave) {
            val now = LocalDateTime.now()
            val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), frameType)
            Path.of("$savePath", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Path.of("$savePath", fileName)
        }

        try {
            if (saveInMemory) {
                val image = Image.openFITS(inputStream)
                onNext(CameraExposureFinished(camera, stepExecution, image, savePath))
            } else {
                LOG.info("saving FITS at $savePath...")

                savePath!!.createParentDirectories()
                inputStream.transferAndClose(savePath.outputStream())

                onNext(CameraExposureFinished(camera, stepExecution, null, savePath))
            }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            aborted.set(true)
        }
    }

    private fun onCameraExposureUpdated(exposureRemainingTime: Duration, exposureProgress: Double) {
        val elapsedTime = (captureElapsedTime + exposureElapsedTime).microseconds
        var captureRemainingTime = Duration.ZERO
        var captureProgress = 0.0

        if (!isLoop) {
            captureRemainingTime = if (captureTime > elapsedTime) captureTime - elapsedTime else Duration.ZERO
            captureProgress = (captureTime - captureRemainingTime) / captureTime
        }

        with(stepExecution!!.executionContext) {
            putLong(EXPOSURE_TIME, exposureTime.inWholeMicroseconds)
            putLong(EXPOSURE_REMAINING_TIME, exposureRemainingTime.inWholeMicroseconds)
            putDouble(EXPOSURE_PROGRESS, exposureProgress)
            putLong(CAPTURE_TIME, captureTime.inWholeMicroseconds)
            putLong(CAPTURE_REMAINING_TIME, captureRemainingTime.inWholeMicroseconds)
            putDouble(CAPTURE_PROGRESS, captureProgress)
            putLong(CAPTURE_ELAPSED_TIME, elapsedTime.inWholeMicroseconds)
        }

        onNext(CameraExposureUpdated(camera, stepExecution!!))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTasklet>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
