package nebulosa.server.equipments.cameras

import io.grpc.stub.StreamObserver
import nebulosa.grpc.CameraExposureTaskResponse
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.protocol.PropertyState
import nebulosa.server.ThreadedTask
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Phaser
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
    val frameFormat: String,
    val frameType: FrameType,
    val binX: Int,
    val binY: Int,
    val save: Boolean = false,
    val savePath: String = "",
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
    val responseObserver: StreamObserver<CameraExposureTaskResponse>,
) : ArrayList<CameraExposureTaskResponse>(64), ThreadedTask, KoinComponent {

    private val eventBus by inject<EventBus>()
    @Volatile private var progress = 0.0
    @Volatile private var state = PropertyState.IDLE
    @Volatile private var imagePath = ""
    @Volatile private var finishedWithError = false
    @Volatile private var aborted = false
    @Volatile private var finished = false
    @Volatile private var remaining = amount
    @Volatile private var capturing = false
    private val phaser = Phaser(1)

    override var startedAt = LocalDateTime.now()!!

    override var finishedAt = LocalDateTime.now()!!

    @Synchronized
    private fun response() {
        CameraExposureTaskResponse.newBuilder()
            .setState(state.name)
            .setProgress(progress)
            .setImagePath(imagePath)
            .setFinishedWithError(finishedWithError)
            .setCapturing(capturing)
            .setAborted(aborted)
            .setFinished(finished)
            .build()
            .also(responseObserver::onNext)
            .also(::add)
    }

    @Synchronized
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraExposureFrame -> {
                capturing = false
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureStateChanged -> {
                state = event.device.exposureState

                when (state) {
                    PropertyState.IDLE -> {
                        // Aborted.
                        if (event.prevState == PropertyState.BUSY) {
                            capturing = false
                            phaser.forceTermination()
                            aborted = true
                            response()
                            finishGracefully()
                        }
                    }
                    PropertyState.BUSY -> {
                        capturing = true
                        progress = ((amount - remaining - 1).toDouble() / amount) +
                                ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)
                        capturing = true
                        response()
                    }
                    PropertyState.ALERT -> {
                        // Failed.
                        capturing = false
                        finishedWithError = true
                        response()
                        finishGracefully()
                    }
                    PropertyState.OK -> Unit
                }
            }
        }
    }

    override fun run() {
        startedAt = LocalDateTime.now()

        camera.enableBlob()

        try {
            eventBus.register(this)

            while (remaining > 0) {
                synchronized(camera) {
                    phaser.register()

                    remaining--

                    camera.frame(x, y, width, height)
                    camera.frameType(frameType)
                    camera.frameFormat(frameFormat)
                    camera.bin(binX, binY)
                    camera.startCapture(exposure)

                    phaser.arriveAndAwaitAdvance()

                    sleep()
                }
            }
        } finally {
            capturing = false
            finishedAt = LocalDateTime.now()
            eventBus.unregister(this)
            finished = true
            capturing = false
            response()
            responseObserver.onCompleted()
        }
    }

    override fun finishGracefully() {
        remaining = 0
    }

    private fun sleep() {
        var remainingTime = delay

        while (remaining > 0 && remainingTime > 0L) {
            Thread.sleep(min(remainingTime, DELAY_INTERVAL))
            remainingTime -= DELAY_INTERVAL
        }
    }

    private fun save(fits: InputStream) {
        if (save && savePath.isNotBlank()) {
            val folderName = autoSubFolderMode.folderName()
            val fileDirectory = Paths.get(savePath, folderName).normalize()
            fileDirectory.createDirectories()
            // TODO: Filter Name
            val fileName = "%s_%s.fits".format(LocalDateTime.now().format(DATE_TIME_FORMAT), frameType)
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving FITS at $filePath...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            imagePath = "$filePath"
        } else {
            val fileDirectory = Paths.get(System.getProperty("java.io.tmpdir"))
            val fileName = "%s.fits".format(camera.name)
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving temporary FITS at $filePath...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            imagePath = "$filePath"
        }

        response()

        imagePath = ""
    }

    companion object {

        private const val DELAY_INTERVAL = 100L

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")
    }
}
