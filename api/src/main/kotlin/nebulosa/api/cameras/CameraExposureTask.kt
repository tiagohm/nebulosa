package nebulosa.api.cameras

import nebulosa.fits.fits
import nebulosa.image.format.ReadableHeader
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.CameraExposureAborted
import nebulosa.indi.device.camera.CameraExposureProgressChanged
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.io.transferAndClose
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch
import okio.sink
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream
import kotlin.math.min

data class CameraExposureTask(
    @JvmField val job: Job,
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
) : Task, CameraEventAware {

    private val latch = CountUpDownLatch()
    private val outputPath = Files.createTempFile(camera.name, ".fits")
    private val formatter = CameraCaptureNamingFormatter(camera)

    @JvmField val exposureTimeInMicroseconds = request.exposureTime.toNanos() / 1000L

    override fun handleCameraEvent(event: CameraEvent) {
        if (event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event)
                }
                is CameraExposureAborted,
                is nebulosa.indi.device.camera.CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                    job.accept(CameraExposureFailed(job, this))
                }
                is CameraExposureProgressChanged -> {
                    // "min" fix possible bug on SVBony exposure time?
                    val remainingTime = min(event.device.exposureTime, request.exposureTime.toNanos() / 1000L)
                    val elapsedTime = exposureTimeInMicroseconds - remainingTime
                    val progress = elapsedTime.toDouble() / exposureTimeInMicroseconds
                    job.accept(CameraExposureElapsed(job, this@CameraExposureTask, elapsedTime, remainingTime, progress))
                }
            }
        }
    }

    override fun run() {
        if (camera.connected) {
            LOG.d { debug("Camera Exposure started. camera={}, request={}", camera, request) }

            latch.countUp()

            job.accept(CameraExposureStarted(job, this))

            with(camera) {
                enableBlob()

                if (request.width > 0 && request.height > 0) {
                    frame(request.x, request.y, request.width, request.height)
                }

                frameType(request.frameType)
                frameFormat(request.frameFormat)
                bin(request.binX, request.binY)
                gain(request.gain)
                offset(request.offset)
                startCapture(request.exposureTime.toNanos() / 1000L)
            }

            latch.await()

            LOG.d { debug("Camera Exposure finished. camera={}, request={}", camera, request) }
        } else {
            LOG.warn("camera not connected. camera={}, request={}", camera, request)
        }

        outputPath.deleteIfExists()
    }

    override fun onCancel(source: CancellationSource) {
        camera.abortCapture()
    }

    private fun save(event: CameraFrameCaptured) {
        try {
            val header = if (event.stream != null) {
                event.stream!!.transferAndClose(outputPath.outputStream())
                null
            } else if (event.image != null) {
                outputPath.sink().use(event.image!!::write)
                event.image?.first()?.header
            } else {
                LOG.warn("invalid event. event={}", event)
                return
            }

            with(request.makeSavePath(header = header)) {
                LOG.d { debug("saving FITS image at {}", this) }
                createParentDirectories()
                outputPath.moveTo(this, true)
                job.accept(CameraExposureFinished(job, this@CameraExposureTask, this))
            }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS image", e)
        } finally {
            latch.countDown()
        }
    }

    private fun CameraStartCaptureRequest.makeSavePath(
        autoSave: Boolean = this.autoSave,
        header: ReadableHeader? = null,
    ): Path {
        require(savePath != null) { "savePath is required" }

        return if (autoSave) {
            val now = LocalDateTime.now(formatter.clock)
            val savePath = autoSubFolderMode.pathFor(savePath, now)
            val format = namingFormat.formatFor(frameType)
            val fileName = formatter.format(format, header ?: outputPath.fits().use { it.first().header })
            Path.of("$savePath", "$fileName.fits")
        } else {
            Path.of("$savePath", "${formatter.camera.name}.fits")
        }
    }

    companion object {

        private val LOG = loggerFor<CameraExposureTask>()
    }
}
