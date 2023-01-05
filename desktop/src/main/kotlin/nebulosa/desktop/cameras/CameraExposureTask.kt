package nebulosa.desktop.cameras

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.equipments.ThreadedTask
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.protocol.PropertyState
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
import kotlin.io.path.deleteIfExists
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
    val savePath: String = "",
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
) : ThreadedTask<Unit>(), Consumer<Any>, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val preferences by inject<Preferences>()

    @Volatile private var progress = 0.0
    @Volatile private var state = PropertyState.IDLE
    @Volatile private var imagePath: Path? = null
    @Volatile private var lastImagePath: Path? = null
    @Volatile private var finishedWithError = false
    @Volatile private var isAborted = false
    @Volatile private var isFinished = false
    @Volatile private var isCapturing = false
    @Volatile private var remaining = amount
    @Volatile private var subscriber: Disposable? = null

    private val phaser = Phaser(1)

    @Synchronized
    private fun reportProgress() {
        eventBus.post(
            CameraExposureTaskProgress(
                this, progress, state, imagePath, finishedWithError,
                isAborted, isFinished, isCapturing, remaining,
            )
        )
    }

    @Synchronized
    override fun accept(event: Any) {
        when (event) {
            is CameraExposureFrame -> {
                isCapturing = false
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureStateChanged -> {
                state = event.device.exposureState

                when (state) {
                    PropertyState.IDLE -> {
                        // Aborted.
                        if (event.previousState == PropertyState.BUSY) {
                            isCapturing = false
                            isAborted = true
                            phaser.forceTermination()
                            reportProgress()
                            finishGracefully()
                        }
                    }
                    PropertyState.BUSY -> {
                        isCapturing = true
                        progress = ((amount - remaining - 1).toDouble() / amount) +
                                ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)
                        reportProgress()
                    }
                    PropertyState.ALERT -> {
                        // Failed.
                        isCapturing = false
                        finishedWithError = true
                        reportProgress()
                        finishGracefully()
                    }
                    PropertyState.OK -> Unit
                }
            }
        }
    }

    override fun execute() {
        camera.enableBlob()

        try {
            subscriber = eventBus
                .filter { it is CameraEvent && it.device === camera }
                .subscribe(this)

            if (filterWheel != null) {
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
            isFinished = true
            reportProgress()
        }
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
        imagePath = if (save && savePath.isNotBlank()) {
            val folderName = autoSubFolderMode.folderName()
            val fileDirectory = Paths.get(savePath, folderName).normalize()
            fileDirectory.createDirectories()
            // TODO: Concat filter name.
            val fileName = "%s-%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            Paths.get("$fileDirectory", fileName)
        } else {
            lastImagePath?.deleteIfExists()
            val fileDirectory = Paths.get(System.getProperty("java.io.tmpdir"))
            val fileName = "%s-%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            Paths.get("$fileDirectory", fileName)
        }

        LOG.info("saving FITS at $imagePath...")
        imagePath!!.outputStream().use { output -> fits.use { it.transferTo(output) } }

        reportProgress()

        lastImagePath = imagePath
        imagePath = null
    }

    companion object {

        private const val DELAY_INTERVAL = 100L

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
    }
}
