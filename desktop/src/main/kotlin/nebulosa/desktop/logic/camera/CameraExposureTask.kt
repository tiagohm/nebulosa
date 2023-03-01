package nebulosa.desktop.logic.camera

import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.filterwheel.FilterWheelMoveTask
import nebulosa.desktop.logic.task.Task
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nom.tam.fits.Fits
import nom.tam.util.FitsOutputStream
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

data class CameraExposureTask(
    override val camera: Camera,
    val exposure: Long,
    val amount: Int,
    val delay: Long,
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

    @Volatile var progress = 0.0
        private set

    @Volatile var remaining = amount
        private set

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus

    private val latch = CountUpDownLatch()
    private val imagePaths = arrayListOf<Path>()

    @Subscribe
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
                remaining = 0
            }
            is CameraExposureProgressChanged -> {
                progress = (amount - remaining - 1).toDouble() / amount +
                        (exposure - camera.exposure).toDouble() / exposure * (1.0 / amount)
            }
        }
    }

    override fun call(): List<Path> {
        try {
            eventBus.post(TaskStarted(this))

            val mount = equipmentManager.selectedMount.get()
            val focuser = equipmentManager.selectedFocuser.get()
            val filterWheel = equipmentManager.selectedFilterWheel.get()

            camera.snoop(listOf(mount, focuser, filterWheel))

            if (filterWheel != null && frameType == FrameType.DARK) {
                if (!filterWheel.connected) {
                    LOG.warn("filter wheel ${filterWheel.name} is disconnected")
                } else {
                    val filterAsShutterPosition = preferences.int("filterWheel.${filterWheel.name}.filterAsShutter")

                    if (filterAsShutterPosition != null) {
                        LOG.info("moving filter wheel ${filterWheel.name} to dark filter")
                        val task = FilterWheelMoveTask(filterWheel, filterAsShutterPosition)
                        task.call()
                    } else {
                        LOG.info("filter wheel ${filterWheel.name} dont have dark filter")
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

            while (camera.connected && remaining > 0) {
                synchronized(camera) {
                    latch.countUp()

                    remaining--

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

                    Task.sleep(delay, latch)
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
            // TODO: Process custom header: val hdu = fits.read().firstOrNull { it is ImageHDU }
            fits.read()

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
