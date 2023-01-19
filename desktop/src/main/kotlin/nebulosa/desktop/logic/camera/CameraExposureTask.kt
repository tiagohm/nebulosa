package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.equipments.ThreadedTask
import nebulosa.desktop.equipments.ThreadedTaskManager
import nebulosa.desktop.filterwheels.FilterWheelMoveTask
import nebulosa.desktop.gui.camera.AutoSubFolderMode
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.cameras.*
import nebulosa.indi.device.filterwheels.FilterWheel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.math.min

data class CameraExposureTask(
    val camera: Camera,
    val filterWheel: FilterWheel?,
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
    val save: Boolean = false,
    val savePath: Path? = null,
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
) : ThreadedTask<List<Path>>(), Consumer<CameraEvent>, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val preferences by inject<Preferences>()

    @Volatile var progress = 0.0
        private set

    @Volatile var remaining = amount
        private set

    private val imageHistory = LinkedList<Path>()

    override fun accept(event: CameraEvent) {
        when (event) {
            is CameraFrameCaptured -> {
                save(event.fits)
                release()
            }
            is CameraExposureAborted,
            is CameraExposureFailed,
            is CameraDetached -> {
                finish()
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
            isCapturing.set(true)

            subscriber = eventBus
                .filterIsInstance<CameraEvent> { it.device === camera }
                .subscribe(this)

            camera.snoop(listOf(filterWheel))

            if (filterWheel != null && frameType == FrameType.DARK) {
                if (!filterWheel.isConnected) {
                    LOG.warn("filter wheel ${filterWheel.name} is disconnected")
                } else {
                    acquire()

                    // Sync filter names.
                    (1..filterWheel.slotCount)
                        .map { filterWheel.filterNameByPosition(it) }
                        .also(filterWheel::filterNames)

                    val position = preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter") ?: -1
                    LOG.info("moving filter wheel ${filterWheel.name} to capture dark frame")
                    val task = FilterWheelMoveTask(filterWheel, position + 1)
                    FilterWheelMoveTask.execute(task) { release() }

                    await()
                }
            }

            LOG.info(
                "starting capture of camera ${camera.name}: x=$x, y=$y, width=$width," +
                        " height=$height, frameType=$frameType, frameFormat=$frameFormat," +
                        " binX=$binX, binY=$binY, gain=$gain, offset=$offset"
            )

            camera.enableBlob()

            while (camera.isConnected && remaining > 0) {
                synchronized(camera) {
                    acquire()

                    remaining--

                    camera.frame(x, y, width, height)
                    camera.frameType(frameType)
                    if (!frameFormat.isNullOrEmpty()) camera.frameFormat(frameFormat)
                    camera.bin(binX, binY)
                    camera.gain(gain)
                    camera.offset(offset)
                    camera.startCapture(exposure)

                    LOG.info("exposuring camera ${camera.name} by $exposure µs")

                    await()

                    sleep()
                }
            }
        } finally {
            isCapturing.set(false)
            subscriber?.dispose()
        }

        return imageHistory
    }

    override fun finishGracefully() {
        remaining = 0
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        camera.abortCapture()
        return false
    }

    private fun sleep() {
        var remainingTime = delay

        while (remaining > 0 && remainingTime > 0L) {
            Thread.sleep(min(remainingTime, DELAY_INTERVAL))
            remainingTime -= DELAY_INTERVAL
        }
    }

    private fun FilterWheel.filterNameByPosition(position: Int): String {
        return preferences.string("filterWheelManager.equipment.$name.filterSlot.$position.label")
            ?.ifBlank { null }
            ?: "Filter#$position"
    }

    @Synchronized
    private fun save(fits: InputStream) {
        val imagePath = if (save) {
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

        if (save) imageHistory.addFirst(imagePath)

        eventBus.post(CameraFrameSaved(camera, imagePath, !save))
    }

    companion object : ThreadedTaskManager<List<Path>, CameraExposureTask>() {

        val isCapturing = SimpleBooleanProperty(false)

        private const val DELAY_INTERVAL = 100L

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    }
}
