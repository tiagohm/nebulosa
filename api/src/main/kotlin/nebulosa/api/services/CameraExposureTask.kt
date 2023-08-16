package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.events.CameraCaptureProgressChanged
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.common.concurrency.ThreadedJob
import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.mount.Mount
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nom.tam.fits.Fits
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.InputStream
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class CameraExposureTask(
    val camera: Camera,
    val exposure: Duration,
    val amount: Int,
    val delay: Duration,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val frameFormat: String?,
    val frameType: FrameType,
    val binX: Int,
    val binY: Int,
    val gain: Int,
    val offset: Int,
    val autoSave: Boolean = false,
    val savePath: Path? = null,
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
    val mount: Mount? = null,
    val filterWheel: FilterWheel? = null,
) : ThreadedJob<Path>() {

    @Volatile var remainingAmount = amount
        private set

    @Volatile var frameRemainingTime = 0L
        private set

    @Volatile var frameProgress = 0f
        private set

    @Volatile var totalRemainingTime = 0L
        private set

    @Volatile var totalProgress = 0f
        private set

    val exposureInMicroseconds = exposure.inWholeMicroseconds
    val indeterminate = amount >= Int.MAX_VALUE

    val totalExposureTime = if (indeterminate) -1L
    else exposureInMicroseconds * amount + (amount - 1) * delay.inWholeMicroseconds

    private val latch = CountUpDownLatch()
    private val forceAbort = AtomicBoolean()

    private var captureStartTime = 0L

    constructor(
        camera: Camera,
        data: CameraStartCaptureRequest,
        savePath: Path,
        mount: Mount? = null,
        filterWheel: FilterWheel? = null,
    ) : this(
        camera,
        data.exposure.toDuration(DurationUnit.MICROSECONDS),
        data.amount,
        data.delay.toDuration(DurationUnit.MILLISECONDS),
        data.x, data.y, data.width, data.height,
        data.frameFormat, data.frameType,
        data.binX, data.binY,
        data.gain, data.offset,
        data.autoSave, savePath, data.autoSubFolderMode,
        mount, filterWheel,
    )

    init {
        LOG.info(
            "camera exposure task. " +
                    "camera={} exposure={} amount={} " +
                    "delay={} x={} y={} width={} height={} " +
                    "frameFormat={} frameType={} binX={} " +
                    "binY={} gain={} offset={} autoSave={} " +
                    "savePath={} autoSubFolderMode={} mount={} " +
                    "filterWheel={}", camera, exposure, amount, delay, x, y,
            width, height, frameFormat, frameType, binX, binY, gain, offset,
            autoSave, savePath, autoSubFolderMode, mount, filterWheel
        )
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        if (running && event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event.fits, event.compressed)
                    latch.countDown()
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                    remainingAmount = 0
                }
                is CameraExposureProgressChanged -> {
                    frameRemainingTime = event.device.exposure
                    frameProgress = (exposureInMicroseconds - frameRemainingTime).toFloat() / exposureInMicroseconds

                    sendProgress()
                }
            }
        }
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
        camera.enableBlob()
        captureStartTime = System.currentTimeMillis()
    }

    override fun execute() {
        if (camera.connected && remainingAmount > 0 && !forceAbort.get()) {
            synchronized(camera) {
                latch.countUp()

                remainingAmount--
                frameRemainingTime = exposureInMicroseconds
                frameProgress = 0f

                camera.frame(x, y, width, height)
                camera.frameType(frameType)
                if (!frameFormat.isNullOrEmpty()) camera.frameFormat(frameFormat)
                camera.bin(binX, binY)
                camera.gain(gain)
                camera.offset(offset)
                camera.startCapture(exposureInMicroseconds)

                LOG.info("exposuring camera ${camera.name} by $exposure")

                latch.await()

                LOG.info("camera exposure finished. abort={}", forceAbort.get())

                if (forceAbort.get()) {
                    stop()
                } else if (remainingAmount > 0) {
                    sleep(delay, forceAbort, ::sendProgress)
                }
            }
        } else {
            stop()
        }
    }

    override fun onStop() {
        EventBus.getDefault().post(CameraCaptureFinished(this))
        EventBus.getDefault().unregister(this)
        camera.disableBlob()
    }

    fun abort() {
        camera.abortCapture()
        forceAbort.set(true)
    }

    private fun save(inputStream: InputStream, compressed: Boolean) {
        val path = if (autoSave) {
            val now = LocalDateTime.now()
            val folderName = autoSubFolderMode.nameAt(now)
            val fileName = "%s-%s.fits".format(now.format(DATE_TIME_FORMAT), frameType)
            val fileDirectory = Path.of("$savePath", folderName).normalize()
            Path.of("$fileDirectory", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Path.of("$savePath", fileName)
        }

        LOG.info("saving FITS at $path...")

        try {
            path.parent.createDirectories()
            inputStream.transferAndClose(path.outputStream())
            add(path)

            Fits(path.toFile()).use {
                val hdu = it.imageHDU(0)!!

                val width = hdu.header.naxis(1)
                val height = hdu.header.naxis(2)
                val mono = Image.isMono(hdu.header)

                val event = SavedCameraImageEntity(
                    0, camera.name, "$path",
                    width, height, mono,
                    exposureInMicroseconds,
                    System.currentTimeMillis(),
                )

                EventBus.getDefault().post(event)
            }
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            abort()
        }
    }

    private fun sendProgress() {
        if (!indeterminate) {
            val elapsedTime = (System.currentTimeMillis() - captureStartTime) * 1000L
            totalRemainingTime = max(0L, totalExposureTime - elapsedTime)
            totalProgress = (totalExposureTime - totalRemainingTime).toFloat() / totalExposureTime
        }

        EventBus.getDefault().post(CameraCaptureProgressChanged(this))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTask>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
