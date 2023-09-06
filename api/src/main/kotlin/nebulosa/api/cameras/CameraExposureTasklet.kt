package nebulosa.api.cameras

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nom.tam.fits.Fits
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.batch.core.ExitStatus
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
import kotlin.math.max

data class CameraExposureTasklet(
    private val camera: Camera,
    private val startCapture: CameraStartCaptureRequest,
) : StoppableTasklet, JobExecutionListener {

    private val latch = CountUpDownLatch()
    private val forceAbort = AtomicBoolean()

    private val exposureInMicroseconds = startCapture.exposureInMicroseconds
    @Volatile private var captureStartTime = 0L
    @Volatile private var jobId = 0L
    @Volatile private var exposureCount = 0

    private val captureTime = if (startCapture.isLoop) -1L
    else exposureInMicroseconds * startCapture.exposureAmount +
            (startCapture.exposureAmount - 1) * startCapture.exposureDelayInSeconds * 1000000L

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
                    forceAbort.set(true)
                }
                is CameraExposureProgressChanged -> {
                    val exposureRemainingTime = event.device.exposure
                    val exposureProgress = (exposureInMicroseconds - exposureRemainingTime).toDouble() / exposureInMicroseconds
                    sendProgress(exposureRemainingTime, exposureProgress, 0.0, 0L, CameraCaptureStatus.CAPTURING)
                }
            }
        }
    }

    @Subscribe
    fun onCameraDelayUpdated(event: CameraDelayUpdated) {
        if (event.camera === camera) {
            sendProgress(0L, 1.0, event.waitProgress, event.waitRemainingTime, CameraCaptureStatus.WAITING)
        }
    }

    override fun beforeJob(jobExecution: JobExecution) {
        EventBus.getDefault().register(this)
        camera.enableBlob()
        jobId = jobExecution.jobId
        sendProgress(startCapture.exposureInMicroseconds, 0.0, 0.0, 0L, CameraCaptureStatus.CAPTURING)
    }

    override fun afterJob(jobExecution: JobExecution) {
        camera.disableBlob()
        EventBus.getDefault().unregister(this)
        EventBus.getDefault().post(CameraCaptureFinished(camera))
        captureStartTime = 0L
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        executeCapture(contribution)
        return RepeatStatus.FINISHED
    }

    override fun stop() {
        LOG.info("stopping exposure. camera=${camera.name}")
        camera.abortCapture()
        forceAbort.set(true)
        latch.reset()
    }

    private fun executeCapture(contribution: StepContribution) {
        if (camera.connected && !forceAbort.get()) {
            synchronized(camera) {
                latch.countUp()

                exposureCount++

                camera.frame(startCapture.x, startCapture.y, startCapture.width, startCapture.height)
                camera.frameType(startCapture.frameType)
                if (!startCapture.frameFormat.isNullOrEmpty()) camera.frameFormat(startCapture.frameFormat)
                camera.bin(startCapture.binX, startCapture.binY)
                camera.gain(startCapture.gain)
                camera.offset(startCapture.offset)
                camera.startCapture(startCapture.exposureInMicroseconds)

                if (captureStartTime == 0L) {
                    captureStartTime = System.currentTimeMillis()
                }

                LOG.info("exposuring camera ${camera.name} by ${startCapture.exposureInMicroseconds}")

                latch.await()

                LOG.info("camera exposure finished")

                if (forceAbort.get()) {
                    contribution.exitStatus = ExitStatus.STOPPED
                }
            }
        } else {
            contribution.exitStatus = ExitStatus.STOPPED
        }
    }

    private fun save(inputStream: InputStream) {
        val fitsPath = if (startCapture.autoSave) {
            val now = LocalDateTime.now()
            val dirName = startCapture.autoSubFolderMode.nameAt(now)
            val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), startCapture.frameType)
            val fileDirectory = Path.of("${startCapture.savePath}", dirName).normalize()
            Path.of("$fileDirectory", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Path.of("${startCapture.savePath}", fileName)
        }

        LOG.info("saving FITS at $fitsPath...")

        try {
            fitsPath.createParentDirectories()
            inputStream.transferAndClose(fitsPath.outputStream())

            Fits(fitsPath.toFile()).use {
                val hdu = it.imageHDU(0)!!

                val width = hdu.header.naxis(1)
                val height = hdu.header.naxis(2)
                val mono = Image.isMono(hdu.header)

                val event = SavedCameraImageEntity(
                    0, camera.name, "$fitsPath",
                    width, height, mono,
                    startCapture.exposureInMicroseconds,
                    System.currentTimeMillis(),
                )

                EventBus.getDefault().post(event)
            }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            forceAbort.set(true)
        }
    }

    private fun sendProgress(
        exposureRemainingTime: Long, exposureProgress: Double,
        waitProgress: Double, waitRemainingTime: Long,
        status: CameraCaptureStatus,
    ) {
        val elapsedTime = (System.currentTimeMillis() - captureStartTime) * 1000L
        var captureRemainingTime = 0L
        var captureProgress = 0.0

        if (!startCapture.isLoop) {
            captureRemainingTime = max(0L, captureTime - elapsedTime)
            captureProgress = (captureTime - captureRemainingTime).toDouble() / captureTime
        }

        val event = CameraExposureUpdated(
            camera, jobId,
            startCapture.exposureAmount, exposureCount,
            startCapture.exposureInMicroseconds, exposureRemainingTime, exposureProgress,
            captureTime, captureRemainingTime, captureProgress,
            startCapture.isLoop, elapsedTime, waitProgress, waitRemainingTime, status,
        )

        EventBus.getDefault().post(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTasklet>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
