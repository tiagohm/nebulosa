package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.filterwheels.FilterWheelMoveTask
import nebulosa.desktop.gui.camera.AutoSubFolderMode
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.taskexecutor.Task
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
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.math.min

data class CameraExposureTask(
    val camera: Camera,
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
    val filterWheel: FilterWheel? = null,
    val filterAsShutterPosition: Int = -1,
) : Task, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val latch = CountUpDownLatch()

    @Volatile var progress = 0.0
        private set

    @Volatile var remaining = amount
        private set

    private fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is CameraFrameCaptured -> {
                save(event.fits)
                latch.countDown()
            }
            is CameraExposureAborted,
            is CameraExposureFailed,
            is CameraDetached -> {
                latch.reset()
                closeGracefully()
            }
            is CameraExposureProgressChanged -> {
                progress = ((amount - remaining - 1).toDouble() / amount) +
                        ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)
            }
        }
    }

    override fun run() {
        var subscriber: Disposable? = null

        try {
            subscriber = eventBus
                .filterIsInstance<CameraEvent> { it.device === camera }
                .subscribe(::onCameraEvent)

            camera.snoop(listOf(filterWheel))

            if (filterWheel != null && frameType == FrameType.DARK) {
                if (!filterWheel.isConnected) {
                    LOG.warn("filter wheel ${filterWheel.name} is disconnected")
                } else {
                    latch.countUp()

                    LOG.info("moving filter wheel ${filterWheel.name} to dark filter")
                    val task = FilterWheelMoveTask(filterWheel, filterAsShutterPosition)
                    FilterWheelMoveTask.execute(task) { latch.countDown() }

                    latch.await()
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

                    sleep()
                }
            }
        } finally {
            subscriber?.dispose()
        }
    }

    override fun closeGracefully() {
        remaining = 0
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

        eventBus.post(CameraFrameSaved(camera, imagePath, autoSave))
    }

    companion object {

        private const val DELAY_INTERVAL = 100L

        @JvmStatic private val LOG = LoggerFactory.getLogger(CameraExposureTask::class.java)
        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    }
}
