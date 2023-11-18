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
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

data class CameraExposureTasklet(override val request: CameraStartCaptureRequest) :
    PublishSequenceTasklet<CameraCaptureEvent>(), CameraStartCaptureTasklet, JobExecutionListener, Consumer<DelayEvent> {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var exposureCount = 0
    @Volatile private var captureElapsedTime = Duration.ZERO!!
    @Volatile private var stepExecution: StepExecution? = null

    private val camera = requireNotNull(request.camera)
    private val exposureTime = request.exposureTime
    private val exposureDelay = request.exposureDelay

    private val estimatedTime = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(exposureTime.toNanos() * request.exposureAmount + exposureDelay.toNanos() * (request.exposureAmount - 1))

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
                    val exposureProgress = exposureElapsedTime.toNanos().toDouble() / exposureTime.toNanos()
                    onCameraExposureElapsed(exposureElapsedTime, exposureRemainingTime, exposureProgress)
                }
            }
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        camera.enableBlob()
        EventBus.getDefault().register(this)
        onNext(CameraCaptureStarted(camera, request.isLoop, estimatedTime, jobExecution, this))
        captureElapsedTime = Duration.ZERO
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
        captureElapsedTime += event.waitDuration
        onNext(CameraCaptureIsWaiting(camera, event.waitDuration, event.remainingTime, event.progress, event.stepExecution, this))
        onCameraExposureElapsed(Duration.ZERO, Duration.ZERO, 1.0)
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

                latch.await()

                captureElapsedTime += exposureTime

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
                val image = Image.open(inputStream)
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

    private fun onCameraExposureElapsed(elapsedTime: Duration, remainingTime: Duration, progress: Double) {
        val totalElapsedTime = captureElapsedTime + elapsedTime
        var captureRemainingTime = Duration.ZERO
        var captureProgress = 0.0

        if (!request.isLoop) {
            captureRemainingTime = if (estimatedTime > totalElapsedTime) estimatedTime - totalElapsedTime else Duration.ZERO
            captureProgress = (estimatedTime - captureRemainingTime).toNanos().toDouble() / estimatedTime.toNanos()
        }

        onNext(CameraExposureElapsed(camera, exposureCount, remainingTime, progress, stepExecution!!, this))
        onNext(CameraCaptureElapsed(camera, exposureCount, captureRemainingTime, captureProgress, totalElapsedTime, stepExecution!!, this))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTasklet>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
