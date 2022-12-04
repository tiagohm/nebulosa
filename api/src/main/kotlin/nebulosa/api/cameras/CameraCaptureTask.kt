package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.scheduler.ScheduledTask
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.FrameFormat
import nebulosa.indi.devices.cameras.FrameType
import nebulosa.indi.devices.events.*
import java.util.concurrent.Phaser

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
) : ScheduledTask<Boolean>() {

    @Volatile private var remaining = amount
    private val phaser = Phaser(1)

    override val name = "${camera.name} Capture"

    internal fun onCameraEventReceived(event: CameraEvent): Any? {
        when (event) {
            is CameraExposureFailedEvent -> {
                finishedWithError = true
                cancel()
            }
            is CameraExposureFrameEvent -> {
                phaser.arriveAndDeregister()
                return CameraCaptureSavedEvent(event.device)
            }
            is CameraExposureAbortedEvent -> {
                phaser.forceTermination()
                cancel()
            }
            is CameraExposureBusyEvent -> {
                progress = ((amount - remaining - 1).toDouble() / amount) +
                    ((exposure - event.exposure).toDouble() / exposure) * (1.0 / amount)
            }
        }

        return null
    }

    override fun execute() {
        camera.enableBlob()

        while (remaining > 0) {
            phaser.register()

            remaining--

            camera.frame(x, y, width, height)
            camera.frameType(frameType)
            camera.frameFormat(frameFormat)
            camera.bin(binX, binY)
            camera.startCapture(exposure)

            phaser.arriveAndAwaitAdvance()

            if (remaining > 0 && delay > 0) {
                Thread.sleep(delay)
            }
        }
    }
}
