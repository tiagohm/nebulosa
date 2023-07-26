package nebulosa.api.services

import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.common.concurrency.ThreadedJob
import nebulosa.fits.FITS_DEC_ANGLE_FORMATTER
import nebulosa.fits.FITS_RA_ANGLE_FORMATTER
import nebulosa.fits.FitsKeywords
import nebulosa.fits.naxis
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import nom.tam.util.FitsOutputStream
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

    @Volatile var remainingTime = 0L
        private set

    val totalExposureTime = exposure.inWholeMilliseconds * amount + (amount - 1) * delay.inWholeMilliseconds

    private val latch = CountUpDownLatch()
    private val forceAbort = AtomicBoolean()

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
                    remainingTime = event.device.exposure
                }
            }
        }
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
        camera.enableBlob()
    }

    override fun execute() {
        if (camera.connected && remainingAmount > 0 && !forceAbort.get()) {
            synchronized(camera) {
                latch.countUp()

                remainingAmount--

                camera.frame(x, y, width, height)
                camera.frameType(frameType)
                if (!frameFormat.isNullOrEmpty()) camera.frameFormat(frameFormat)
                camera.bin(binX, binY)
                camera.gain(gain)
                camera.offset(offset)
                camera.startCapture(exposure.inWholeMicroseconds)

                LOG.info("exposuring camera ${camera.name} by $exposure")

                latch.await()

                LOG.info("camera exposure finished. abort={}", forceAbort.get())

                if (forceAbort.get()) {
                    stop()
                } else if (remainingAmount > 0) {
                    sleep(delay, forceAbort)
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
            Fits(inputStream).use { fits ->
                val hdu = fits.read().firstOrNull { it is ImageHDU }

                if (hdu != null) {
                    val header = hdu.header.also {
                        val mount = mount ?: return@also

                        val raStr = mount.rightAscensionJ2000.format(FITS_RA_ANGLE_FORMATTER)
                        val decStr = mount.declinationJ2000.format(FITS_DEC_ANGLE_FORMATTER)

                        it.addValue(FitsKeywords.RA, raStr)
                        it.addValue(FitsKeywords.OBJCTRA, raStr)
                        it.addValue(FitsKeywords.DEC, decStr)
                        it.addValue(FitsKeywords.OBJCTDEC, decStr)
                    }

                    path.parent.createDirectories()
                    path.outputStream().use { fits.write(FitsOutputStream(it)) }

                    add(path)

                    val width = header.naxis(1)
                    val height = header.naxis(2)
                    val mono = Image.isMono(header)

                    val event = SavedCameraImageEntity(
                        0, camera.name, "$path",
                        width, height, mono,
                        exposure.inWholeMicroseconds,
                        System.currentTimeMillis(),
                    )

                    EventBus.getDefault().post(event)
                } else {
                    LOG.warn("FITS does not contains an image")
                }
            }
        } catch (e: Throwable) {
            LOG.error("failed to read FITS", e)
            forceAbort.set(true)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraExposureTask>()
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmssSSS")
    }
}
