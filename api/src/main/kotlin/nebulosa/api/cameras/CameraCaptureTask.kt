package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.scheduler.ScheduledTask
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.FrameFormat
import nebulosa.indi.devices.cameras.FrameType
import nebulosa.indi.devices.events.*
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
            is CameraExposureFailedEvent -> {
                capturing.set(false)
                finishedWithError = true
                cancel()
            }
            is CameraExposureFrameEvent -> {
                capturing.set(false)
                phaser.arriveAndDeregister()
                save(event.fits)
            }
            is CameraExposureAbortedEvent -> {
                capturing.set(false)
                phaser.forceTermination()
                cancel()
            }
            is CameraExposureBusyEvent -> {
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

        while (remaining.get() > 0) {
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

        if (capturing.compareAndSet(true, false)) {
            camera.handler.fireOnEventReceived(CameraCaptureFinishedEvent(camera))
        }
    }

    private fun save(fits: InputStream) {
        if (save && savePath.isNotBlank()) {
            val subFolderName = autoSubFolderMode.subFolderName()
            // TODO: Filter Name
            val fileName = "%d_%s.fits".format(System.currentTimeMillis(), frameType)
            val fileDirectory = Paths.get(savePath, subFolderName).normalize()
            fileDirectory.createDirectories()
            val filePath = Paths.get("$fileDirectory", fileName)
            println("Saving FITS at $fileDirectory...")
            filePath.outputStream().use { output -> fits.use { it.transferTo(output) } }
            camera.handler.fireOnEventReceived(CameraCaptureSavedEvent(this, filePath))
        }
    }
}
