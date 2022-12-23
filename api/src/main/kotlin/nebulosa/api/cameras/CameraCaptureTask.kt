package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.scheduler.ScheduledTask
import nebulosa.indi.devices.cameras.*
import java.io.InputStream
import java.nio.file.Paths
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

data class CameraCaptureTask(
    @JsonIgnore val camera: Camera,
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
) : ScheduledTask<Boolean>() {

    private val remaining = AtomicInteger(amount)
    private val capturing = AtomicBoolean(false)
    private val phaser = Phaser(1)

    override val name = "${camera.name} Capture"

    internal fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraExposureFailed -> {
                capturing.set(false)
                finishedWithError = true
                cancel()
            }
            is CameraExposureFrame -> {
                capturing.set(false)
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureAborted -> {
                capturing.set(false)
                phaser.forceTermination()
                cancel()
            }
            is CameraExposureStateChanged -> {
                if (capturing.compareAndSet(false, true)) {
                    event.device.handler.fireOnEventReceived(CameraCaptureStartedEvent(event.device))
                }

                progress = ((amount - remaining.get() - 1).toDouble() / amount) +
                    ((exposure - event.exposure).toDouble() / exposure) * (1.0 / amount)
            }
        }
    }

    override fun execute() {
        camera.enableBlob()

        camera["isCapturing"] = true

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

                if (remaining.get() > 0 && delay > 0) {
                    Thread.sleep(delay)
                }
            }
        }

        if (capturing.compareAndSet(true, false)) {
            camera.handler.fireOnEventReceived(CameraCaptureFinishedEvent(camera))
        }
    }

    override fun finishGracefully() {
        camera["isCapturing"] = false
        remaining.set(0)
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
            camera.handler.fireOnEventReceived(CameraCaptureSavedEvent(this, filePath))
        } else {
            val fileDirectory = Paths.get(System.getProperty("java.io.tmpdir"))
            val fileName = "%s.fits".format(camera.name)
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving temporary FITS at $filePath...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            camera.handler.fireOnEventReceived(CameraCaptureSavedEvent(this, filePath, true))
        }
    }
}
