package nebulosa.api.cameras

import nebulosa.api.scheduler.ScheduledTask
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.FrameFormat
import nebulosa.indi.devices.cameras.FrameType
import nebulosa.indi.devices.events.*
import java.util.concurrent.Phaser

class CameraCaptureTask(
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
) : ScheduledTask<Boolean>() {

    @Volatile private var remaining = amount
    private val phaser = Phaser(1)

    override val name = "${camera.name} Capture"

    override val data = mapOf(
        "exposure" to exposure,
        "amount" to amount,
        "delay" to delay,
        "x" to x,
        "y" to y,
        "width" to width,
        "height" to height,
        "frameFormat" to frameFormat,
        "frameType" to frameType,
        "binX" to binX,
        "binY" to binY,
    )

    internal fun onCameraEventReceived(event: CameraEvent) {
        println("$name: $event")

        when (event) {
            is CameraExposureFailedEvent -> {
                finishedWithError = true
                cancel()
            }
            is CameraExposureOkEvent -> {
                phaser.arriveAndDeregister()
            }
            is CameraExposureAbortedEvent -> {
                phaser.forceTermination()
                cancel()
            }
            is CameraExposureBusyEvent -> {
                progress = ((amount - remaining - 1).toDouble() / amount) +
                    ((exposure - event.exposure).toDouble() / exposure) * (1.0 / amount)
                println(progress)
            }
        }
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
