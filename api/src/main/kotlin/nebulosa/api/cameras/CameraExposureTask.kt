package nebulosa.api.cameras

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.camera.*
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import okio.sink
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

data class CameraExposureTask(
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
) : Task<CameraExposureEvent>(), CancellationListener {

    private val latch = CountUpDownLatch()
    private val aborted = AtomicBoolean()

    @Volatile private var state = CameraExposureState.IDLE
    @Volatile private var elapsedTime = Duration.ZERO
    @Volatile private var remainingTime = Duration.ZERO
    @Volatile private var progress = 0.0
    @Volatile private var savedPath: Path? = null

    fun handleCameraEvent(event: CameraEvent) {
        if (event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event)
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    onCancelled(CancellationSource.Close)
                }
                is CameraExposureProgressChanged -> {
                    val exposureTime = request.exposureTime
                    // minOf fix possible bug on SVBony exposure time?
                    remainingTime = minOf(event.device.exposureTime, request.exposureTime)
                    elapsedTime = exposureTime - remainingTime
                    progress = elapsedTime.toNanos().toDouble() / exposureTime.toNanos()
                    state = CameraExposureState.ELAPSED
                    sendEvent()
                }
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (camera.connected && !aborted.get()) {
            LOG.info("camera exposure started. camera={}, request={}", camera, request)

            latch.countUp()

            state = CameraExposureState.STARTED
            sendEvent()

            with(camera) {
                if (request.width > 0 && request.height > 0) {
                    frame(request.x, request.y, request.width, request.height)
                }

                frameType(request.frameType)
                frameFormat(request.frameFormat)
                bin(request.binX, request.binY)
                gain(request.gain)
                offset(request.offset)
                startCapture(exposureTime)
            }

            latch.await()

            if (aborted.get()) {
                state = CameraExposureState.ABORTED
                sendEvent()
            }

            LOG.info("camera exposure finished. camera={}, request={}", camera, request)
        } else {
            LOG.warn("camera not connected or aborted. aborted={}, camera={}, request={}", aborted.get(), camera, request)
        }
    }

    override fun onCancelled(source: CancellationSource) {
        aborted.set(true)
        latch.reset()
    }

    override fun reset() {
        aborted.set(false)
        latch.reset()
    }

    override fun close() {
        onCancelled(CancellationSource.Close)
        super.close()
    }

    private fun save(event: CameraFrameCaptured) {
        try {
            val savedPath = request.makeSavePath(event.device)

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

            this.savedPath = savedPath
            state = CameraExposureState.FINISHED

            sendEvent()
        } catch (e: Throwable) {
            LOG.error("failed to save FITS image", e)
            aborted.set(true)
        } finally {
            latch.countDown()
        }
    }

    private fun sendEvent() {
        onNext(CameraExposureEvent(this, state, elapsedTime, remainingTime, progress, savedPath))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTask>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")

        @JvmStatic
        internal fun CameraStartCaptureRequest.makeSavePath(
            camera: Camera, autoSave: Boolean = this.autoSave,
        ): Path {
            require(savePath != null) { "savePath is required" }

            return if (autoSave) {
                val now = LocalDateTime.now()
                val savePath = autoSubFolderMode.pathFor(savePath, now)
                val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), frameType)
                Path.of("$savePath", fileName)
            } else {
                val fileName = "%s.fits".format(camera.name)
                Path.of("$savePath", fileName)
            }
        }
    }
}
