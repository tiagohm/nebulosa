package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.sequencer.PublishSequenceTasklet
import nebulosa.api.sequencer.tasklets.delay.DelayEvent
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
import kotlin.time.Duration.Companion.seconds

data class CameraExposureTasklet(override val request: CameraStartCaptureRequest) :
    PublishSequenceTasklet<CameraCaptureEvent>(), CameraStartCaptureTasklet, JobExecutionListener, Consumer<DelayEvent> {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = 0L
    @Volatile private var exposureElapsedTime = 0L
    @Volatile private var stepExecution: StepExecution? = null

    private val camera = requireNotNull(request.camera)
    private val exposureTime = request.exposureInMicroseconds.microseconds
    private val exposureDelay = request.exposureDelayInSeconds.seconds
    private val totalTime = if (request.isLoop) Duration.ZERO
    else exposureTime * request.exposureAmount + exposureDelay * (request.exposureAmount - 1)

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
        onNext(CameraCaptureStarted(camera, request.isLoop, totalTime, jobExecution, this))
        captureElapsedTime = 0L
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        EventBus.getDefault().unregister(this)
        onNext(CameraCaptureFinished(camera, jobExecution, this))
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

    override fun accept(event: DelayEvent) {
        captureElapsedTime += event.waitDuration.inWholeMicroseconds

        onNext(
            CameraCaptureIsWaiting(
                camera, event.waitDuration.inWholeMicroseconds, event.remainingTime.inWholeMicroseconds,
                event.progress, event.stepExecution, this
            )
        )

        onCameraExposureUpdated(Duration.ZERO, 1.0)
    }

    private fun executeCapture(contribution: StepContribution) {
        stepExecution = contribution.stepExecution

        if (camera.connected && !aborted.get()) {
            synchronized(camera) {
                latch.countUp()

                exposureCount++

                onNext(CameraExposureStarted(camera, exposureCount, stepExecution!!, this))

                if (request.width > 0 && request.height > 0) {
                    camera.frame(request.x, request.y, request.width, request.height)
                }

                camera.frameType(request.frameType)
                camera.frameFormat(request.frameFormat)
                camera.bin(request.binX, request.binY)
                camera.gain(request.gain)
                camera.offset(request.offset)
                camera.startCapture(exposureTime)

                exposureElapsedTime = 0L

                latch.await()

                exposureElapsedTime = 0L
                captureElapsedTime += exposureTime.inWholeMicroseconds

                LOG.info("camera exposure finished")
            }
        }
    }

    private fun save(inputStream: InputStream, stepExecution: StepExecution) {
        val savePath = if (request.saveInMemory) {
            request.savePath
        } else if (request.autoSave) {
            val now = LocalDateTime.now()
            val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), request.frameType)
            Path.of("${request.savePath}", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Path.of("${request.savePath}", fileName)
        }

        try {
            if (request.saveInMemory) {
                val image = Image.openFITS(inputStream)
                onNext(CameraExposureFinished(camera, exposureCount, stepExecution, this, image, savePath))
            } else {
                LOG.info("saving FITS at $savePath...")

                savePath!!.createParentDirectories()
                inputStream.transferAndClose(savePath.outputStream())

                onNext(CameraExposureFinished(camera, exposureCount, stepExecution, this, null, savePath))
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

        if (!request.isLoop) {
            captureRemainingTime = if (totalTime > elapsedTime) totalTime - elapsedTime else Duration.ZERO
            captureProgress = (totalTime - captureRemainingTime) / totalTime
        }

        onNext(
            CameraExposureElapsed(
                camera, exposureCount,
                exposureRemainingTime.inWholeMicroseconds, exposureProgress,
                stepExecution!!, this
            )
        )

        onNext(
            CameraCaptureElapsed(
                camera, exposureCount,
                captureRemainingTime.inWholeMicroseconds, captureProgress,
                stepExecution!!, this
            )
        )
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTasklet>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
