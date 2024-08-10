package nebulosa.api.cameras

import nebulosa.api.tasks.AbstractTask
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.fits.fits
import nebulosa.image.format.ReadableHeader
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import okio.sink
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createParentDirectories
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

data class CameraExposureTask(
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
) : AbstractTask<CameraExposureEvent>(), CancellationListener, CameraEventAware {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var elapsedTime = Duration.ZERO
    @Volatile private var remainingTime = Duration.ZERO
    @Volatile private var progress = 0.0
    @Volatile private var savedPath: Path? = null

    private val outputPath = Files.createTempFile(camera.name, ".fits")
    private val formatter = CameraCaptureNamingFormatter(camera)

    val isAborted
        get() = aborted.get()

    override fun handleCameraEvent(event: CameraEvent) {
        if (event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event)
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    aborted.set(true)
                    latch.reset()
                }
                is CameraExposureProgressChanged -> {
                    val exposureTime = request.exposureTime
                    // minOf fix possible bug on SVBony exposure time?
                    remainingTime = minOf(event.device.exposureTime, request.exposureTime)
                    elapsedTime = exposureTime - remainingTime
                    progress = elapsedTime.toNanos().toDouble() / exposureTime.toNanos()
                    sendEvent(CameraExposureState.ELAPSED)
                }
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (camera.connected && !aborted.get()) {
            LOG.info("Camera Exposure started. camera={}, request={}", camera, request)

            cancellationToken.waitForPause()

            latch.countUp()

            sendEvent(CameraExposureState.STARTED)

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
                startCapture(request.exposureTime)
            }

            try {
                cancellationToken.listen(this)
                latch.await()
            } finally {
                cancellationToken.unlisten(this)
            }

            LOG.info("Camera Exposure finished. aborted={}, camera={}, request={}", aborted.get(), camera, request)
        } else {
            LOG.warn("camera not connected or aborted. aborted={}, camera={}, request={}", aborted.get(), camera, request)
        }
    }

    override fun onCancel(source: CancellationSource) {
        camera.abortCapture()
    }

    override fun reset() {
        aborted.set(false)
        latch.reset()
    }

    override fun close() {
        onCancel(CancellationSource.Close)
        super.close()
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
                LOG.info("saving FITS image at {}", this)
                createParentDirectories()
                outputPath.moveTo(this, true)
                savedPath = this
            }

            sendEvent(CameraExposureState.FINISHED)
        } catch (e: Throwable) {
            LOG.error("failed to save FITS image", e)
            aborted.set(true)
        } finally {
            latch.countDown()
        }
    }

    private fun sendEvent(state: CameraExposureState) {
        onNext(CameraExposureEvent(this, state, elapsedTime, remainingTime, progress, savedPath))
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

        @JvmStatic private val LOG = loggerFor<CameraExposureTask>()
    }
}
