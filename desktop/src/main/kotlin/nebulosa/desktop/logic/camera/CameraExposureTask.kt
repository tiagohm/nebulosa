package nebulosa.desktop.logic.camera

import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.filterwheel.FilterWheelMoveTask
import nebulosa.desktop.logic.filterwheel.filterName
import nebulosa.desktop.logic.task.Task
import nebulosa.desktop.logic.task.TaskExecutor
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.fits.FITS_DEC_ANGLE_FORMATTER
import nebulosa.fits.FITS_RA_ANGLE_FORMATTER
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.ObservationDescription
import nom.tam.fits.header.extra.SBFitsExt
import nom.tam.util.FitsOutputStream
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

data class CameraExposureTask(
    override val camera: Camera,
    val exposure: Long, // us
    val amount: Int,
    val delay: Long, // ms
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
) : CameraTask {

    @Volatile var remainingAmount = amount
        private set

    @Volatile var remainingTime = 0L
        private set

    val elapsedTime
        get() = (System.nanoTime() - stopWatch) / 1000L

    val totalExposureTime = exposure * amount + (amount - 1) * delay * 1000L

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var taskExecutor: TaskExecutor

    private val latch = CountUpDownLatch()
    private val imagePaths = arrayListOf<Path>()
    private val forceAbort = AtomicBoolean()

    @Volatile private var stopWatch = 0L

    private val mount: Mount?
        get() = equipmentManager.selectedMount.get()

    private val focuser: Focuser?
        get() = equipmentManager.selectedFocuser.get()

    private val filterWheel: FilterWheel?
        get() = equipmentManager.selectedFilterWheel.get()

    val filterName
        get() = filterWheel?.let { preferences.filterName(it) }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: CameraEvent) {
        if (event.device !== camera) return

        when (event) {
            is CameraFrameCaptured -> {
                imagePaths.add(save(event.fits))
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

    override fun call(): List<Path> {
        try {
            eventBus.post(TaskStarted(this))

            camera.snoop(listOf(mount, focuser, filterWheel))

            if (frameType == FrameType.DARK) {
                filterWheel?.also {
                    val useFilterAsShutter = preferences.bool("filterWheel.${it.name}.useFilterWheelAsShutter")

                    if (useFilterAsShutter) {
                        if (!it.connected) {
                            LOG.warn("filter wheel ${it.name} is disconnected")
                        } else {
                            val filterAsShutterPosition = preferences.int("filterWheel.${it.name}.filterAsShutter")

                            if (filterAsShutterPosition != null) {
                                LOG.info("moving filter wheel ${it.name} to dark filter")
                                val task = FilterWheelMoveTask(it, filterAsShutterPosition)
                                taskExecutor.execute(task).get()
                            } else {
                                LOG.info("filter wheel ${it.name} dont have dark filter")
                            }
                        }
                    }
                }
            }

            LOG.info(
                "starting capture of camera ${camera.name}: x=$x, y=$y, width=$width," +
                        " height=$height, frameType=$frameType, frameFormat=$frameFormat," +
                        " binX=$binX, binY=$binY, gain=$gain, offset=$offset"
            )

            camera.enableBlob()

            eventBus.register(this)

            stopWatch = System.nanoTime()

            while (camera.connected && remainingAmount > 0 && !forceAbort.get()) {
                synchronized(camera) {
                    latch.countUp()

                    remainingAmount--

                    camera.frame(x, y, width, height)
                    camera.frameType(frameType)
                    if (!frameFormat.isNullOrEmpty()) camera.frameFormat(frameFormat)
                    camera.bin(binX, binY)
                    camera.gain(gain)
                    camera.offset(offset)
                    camera.startCapture(exposure)

                    LOG.info("exposuring camera ${camera.name} by $exposure µs")

                    latch.await()

                    LOG.info("camera exposure finished")

                    if (forceAbort.get()) {
                        return@synchronized
                    } else if (remainingAmount > 0) {
                        Task.sleep(delay, forceAbort)
                    }
                }
            }
        } catch (e: Throwable) {
            LOG.error("camera exposure failed.", e)
            throw e
        } finally {
            eventBus.unregister(this)
            eventBus.post(TaskFinished(this))
        }

        return imagePaths
    }

    fun abort() {
        camera.abortCapture()
        forceAbort.set(true)
    }

    @Synchronized
    private fun save(inputStream: InputStream): Path {
        val path = if (autoSave) {
            val folderName = autoSubFolderMode.folderName()
            val fileName = "%s-%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            val fileDirectory = Paths.get("$savePath", folderName).normalize()
            Paths.get("$fileDirectory", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Paths.get("$savePath", fileName)
        }

        LOG.info("saving FITS at $path...")

        Fits(inputStream).use { fits ->
            val hdu = fits.read().firstOrNull { it is ImageHDU }

            hdu?.header?.also {
                val mount = mount ?: return@also

                val raStr = mount.rightAscensionJ2000.format(FITS_RA_ANGLE_FORMATTER)
                val decStr = mount.declinationJ2000.format(FITS_DEC_ANGLE_FORMATTER)

                it.addValue(ObservationDescription.RA, raStr)
                it.addValue(SBFitsExt.OBJCTRA, raStr)
                it.addValue(ObservationDescription.DEC, decStr)
                it.addValue(SBFitsExt.OBJCTDEC, decStr)
            }

            path.parent.createDirectories()
            path.outputStream().use { fits.write(FitsOutputStream(it)) }

            val image = Image.open(fits)

            eventBus.post(CameraFrameSaved(this, image, path, autoSave))
        }

        return path
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    }
}
