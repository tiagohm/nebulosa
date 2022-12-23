package nebulosa.server.cameras

import io.grpc.stub.StreamObserver
import nebulosa.grpc.CameraExposureTaskResponse
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.protocol.PropertyState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream
import java.nio.file.Paths
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

data class CameraExposureTask(
    val camera: Camera,
    val exposure: Long,
    val amount: Int,
    val delay: Long,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val frameFormat: FrameFormat,
    val frameType: FrameType,
    val binX: Int,
    val binY: Int,
    val save: Boolean = false,
    val savePath: String = "",
    val autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
    val responseObserver: StreamObserver<CameraExposureTaskResponse>,
) : ArrayList<CameraExposureTaskResponse>(64), Runnable, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val remaining = AtomicInteger(amount)
    private val capturing = AtomicBoolean(false)
    private val phaser = Phaser(1)

    @Synchronized
    private fun response(
        state: PropertyState? = null,
        progress: Double? = null,
        imagePath: String? = null,
        finishedWithError: Boolean? = null,
        capturing: Boolean? = null,
        aborted: Boolean? = null,
        finished: Boolean? = null,
    ) {
        val response = firstOrNull()?.newBuilderForType()
            ?: CameraExposureTaskResponse.newBuilder()

        response
            .setState(if (state != null) nebulosa.grpc.PropertyState.valueOf(state.name) else response.state)
            .setProgress(progress ?: response.progress)
            .setImagePath(imagePath ?: response.imagePath)
            .setFinishedWithError(finishedWithError ?: response.finishedWithError)
            .setCapturing(capturing ?: response.capturing)
            .setAborted(aborted ?: response.aborted)
            .setFinished(finished ?: response.finished)
            .build()
            .also(responseObserver::onNext)
            .also(::add)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraExposureFrame -> {
                capturing.set(false)
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureStateChanged -> {
                when (val state = event.device.exposureState) {
                    PropertyState.IDLE -> {
                        // Aborted.
                        if (event.prevState == PropertyState.BUSY) {
                            capturing.set(false)
                            phaser.forceTermination()
                            response(state, aborted = true)
                            finishGracefully()
                        }
                    }
                    PropertyState.BUSY -> {
                        capturing.set(true)

                        val progress = ((amount - remaining.get() - 1).toDouble() / amount) +
                            ((exposure - camera.exposure).toDouble() / exposure) * (1.0 / amount)

                        response(state, progress = progress, capturing = true)
                    }
                    PropertyState.ALERT -> {
                        // Failed.
                        capturing.set(false)
                        response(state, finishedWithError = true)
                        finishGracefully()
                    }
                    PropertyState.OK -> Unit
                }
            }
        }
    }

    override fun run() {
        camera.enableBlob()

        eventBus.register(this)

        try {
            while (remaining.get() > 0) {
                synchronized(camera) {
                    phaser.register()

                    remaining.decrementAndGet()

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
            capturing.set(false)
            eventBus.unregister(this)
            response(finished = true, capturing = false)
        }
    }

    fun finishGracefully() {
        remaining.set(0)
    }

    private fun sleep() {
        var remainingTime = delay

        while (remaining.get() > 0 && remainingTime > 0L) {
            Thread.sleep(1000L)
            remainingTime -= 1000L
        }
    }

    private fun save(fits: InputStream) {
        if (save && savePath.isNotBlank()) {
            val subFolderName = autoSubFolderMode.subFolderName()
            val fileDirectory = Paths.get(savePath, subFolderName).normalize()
            fileDirectory.createDirectories()
            // TODO: Filter Name
            val fileName = "%d_%s.fits".format(System.currentTimeMillis(), frameType)
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving FITS at $filePath...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            response(imagePath = "$filePath")
        } else {
            val fileDirectory = Paths.get(System.getProperty("java.io.tmpdir"))
            val fileName = "%s.fits".format(camera.name)
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving temporary FITS at $filePath...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            response(imagePath = "$filePath")
        }
    }
}
