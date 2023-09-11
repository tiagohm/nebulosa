package nebulosa.api.cameras

import nebulosa.api.tasklets.delay.DelayListener
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
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
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
    private val listener: CameraCaptureListener? = null,
    private val saveInMemory: Boolean = false,
) : StoppableTasklet, JobExecutionListener, DelayListener {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = 0L
    @Volatile private var exposureElapsedTime = 0L

    private val isLoop = exposureAmount <= 0
    private val captureTime = if (isLoop) Duration.ZERO else exposureTime * exposureAmount + exposureDelay * (exposureAmount - 1)

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
                    aborted.set(true)
                }
                is CameraExposureProgressChanged -> {
                    if (listener != null) {
                        val exposureRemainingTime = event.device.exposureTime
                        val exposureElapsedTime = exposureTime - exposureRemainingTime
                        this.exposureElapsedTime = exposureElapsedTime.inWholeMicroseconds

                        val exposureProgress = exposureElapsedTime / exposureTime
                        onCameraExposureUpdated(exposureRemainingTime, exposureProgress)
                    }
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        camera.enableBlob()
        EventBus.getDefault().register(this)
        listener?.onCameraCaptureStarted(camera)
        captureElapsedTime = 0L
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        EventBus.getDefault().unregister(this)
        listener?.onCameraCaptureFinished(camera)
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        executeCapture()
        return RepeatStatus.FINISHED
    }

    override fun stop() {
        LOG.info("stopping exposure. camera=${camera.name}")
        camera.abortCapture()
        camera.disableBlob()
        aborted.set(true)
        latch.reset()
    }

    override fun onDelayElapsed(remainingTime: Duration, delayTime: Duration, waitTime: Duration) {
        if (listener != null) {
            captureElapsedTime += waitTime.inWholeMicroseconds
            val waitProgress = if (remainingTime > Duration.ZERO) 1.0 - delayTime / remainingTime else 1.0
            listener.onCameraExposureDelayElapsed(camera, waitProgress, remainingTime, delayTime)
            onCameraExposureUpdated(Duration.ZERO, 1.0)
        }
    }

    private fun executeCapture() {
        if (camera.connected && !aborted.get()) {
            synchronized(camera) {
                latch.countUp()

                exposureCount++

                listener?.onCameraExposureStarted(camera, exposureCount)

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

                if (listener != null) {
                    exposureElapsedTime = 0L
                    captureElapsedTime += exposureTime.inWholeMicroseconds
                }

                LOG.info("camera exposure finished")
            }
        }
    }

    private fun save(inputStream: InputStream) {
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
                listener?.onCameraExposureFinished(camera, image, savePath)
            } else {
                LOG.info("saving FITS at $savePath...")

                savePath!!.createParentDirectories()
                inputStream.transferAndClose(savePath.outputStream())
                listener?.onCameraExposureFinished(camera, null, savePath)
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

        listener!!.onCameraExposureUpdated(
            camera,
            exposureAmount, exposureCount,
            exposureTime, exposureRemainingTime, exposureProgress,
            captureTime, captureRemainingTime, captureProgress,
            isLoop, elapsedTime,
        )
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTasklet>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
