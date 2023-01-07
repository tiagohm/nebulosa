package nebulosa.desktop.cameras

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.equipments.ThreadedTask
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.devices.filterwheels.FilterWheel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Phaser
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
    val frameFormat: String,
    val frameType: FrameType,
    val binX: Int,
    val binY: Int,
    val gain: Int,
    val offset: Int,
    val save: Boolean = false,
    val savePath: Path? = null,
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
) : ThreadedTask<List<Path>>(), Consumer<Any>, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val preferences by inject<Preferences>()

    @Volatile var isCapturing = false
        private set

    @Volatile var progress = 0.0
        private set

    @Volatile var remaining = amount
        private set

    @Volatile private var imagePath: Path? = null
    @Volatile private var subscriber: Disposable? = null
    private val imageHistory = ArrayList<Path>(min(1000, amount))

    private val phaser = Phaser(1)

    @Synchronized
    override fun accept(event: Any) {
        when (event) {
            is CameraFrameCaptured -> {
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureAborted,
            is CameraExposureFailed -> {
                phaser.forceTermination()
                finishGracefully()
            }
            is CameraExposureProgressChanged -> {
                progress = ((amount - remaining - 1).toDouble() / amount) +
                        ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)
            }
        }
    }

    override fun get(): List<Path> {
        camera.enableBlob()

        isCapturing = true

        try {
            subscriber = eventBus
                .filter { it is CameraEvent && it.device === camera }
                .subscribe(this)

            if (filterWheel != null && frameType == FrameType.DARK) {
                synchronized(filterWheel) {
                    val selectedFilterAsShutter = preferences.int("filterWheelManager.equipment.${filterWheel.name}.filterAsShutter") ?: -1
                    filterWheel.moveTo(selectedFilterAsShutter)
                }
            }

            while (remaining > 0) {
                synchronized(camera) {
                    phaser.register()

                    remaining--

                    camera.frame(x, y, width, height)
                    camera.frameType(frameType)
                    camera.frameFormat(frameFormat)
                    camera.bin(binX, binY)
                    camera.gain(gain)
                    camera.offset(offset)
                    camera.startCapture(exposure)

                    phaser.arriveAndAwaitAdvance()

                    sleep()
                }
            }
        } finally {
            isCapturing = false

            subscriber?.dispose()
            subscriber = null
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

    @Synchronized
    private fun save(fits: InputStream) {
        imagePath = if (save) {
            val folderName = autoSubFolderMode.folderName()
            val fileName = "%s-%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            val fileDirectory = Paths.get("$savePath", folderName).normalize()
            Paths.get("$fileDirectory", fileName)
        } else {
            val fileName = "%s.fits".format(camera.name)
            Paths.get("$savePath", fileName)
        }

        LOG.info("saving FITS at $imagePath...")

        imagePath!!.parent.createDirectories()
        imagePath!!.outputStream().use { output -> fits.use { it.transferTo(output) } }

        eventBus.post(CameraFrameSaved(camera, imagePath!!, !save))
    }

    companion object {

        private const val DELAY_INTERVAL = 100L

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    }
}
