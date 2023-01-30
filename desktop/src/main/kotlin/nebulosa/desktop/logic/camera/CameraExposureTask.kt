package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.filterwheel.FilterWheelMoveTask
import nebulosa.desktop.logic.task.Task
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
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
) : CameraTask, KoinComponent {

    @Volatile var progress = 0.0
        private set

    @Volatile var remaining = amount
        private set

    private val equipmentManager by inject<EquipmentManager>()
    private val preferences by inject<Preferences>()
    private val latch = CountUpDownLatch()
    private val imagePaths = arrayListOf<Path>()

    private fun onEvent(event: DeviceEvent<*>) {
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
                progress = ((amount - remaining - 1).toDouble() / amount) +
                        ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)
            }
        }
    }

    override fun call(): List<Path> {
        var subscriber: Disposable? = null

        try {
            subscriber = EventBus.DEVICE
                .subscribe(filter = { it.device === camera }, next = ::onEvent)

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
                        FilterWheelMoveTask(filterWheel, filterAsShutterPosition).call()
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
        } finally {
            subscriber?.dispose()
        }

        return imagePaths
    }

    @Synchronized
    private fun save(fits: InputStream): Path {
        val imagePath = if (autoSave) {
            val folderName = autoSubFolderMode.folderName()
            val fileName = "%s-%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            val fileDirectory = Paths.get("$savePath", folderName).normalize()
            Paths.get("$fileDirectory", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Paths.get("$savePath", fileName)
        }

        LOG.info("saving FITS at $imagePath...")

        imagePath.parent.createDirectories()
        imagePath.outputStream().use { output -> fits.use { it.transferTo(output) } }

        EventBus.TASK.post(CameraFrameSaved(this, imagePath, autoSave))

        return imagePath
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    }
}
