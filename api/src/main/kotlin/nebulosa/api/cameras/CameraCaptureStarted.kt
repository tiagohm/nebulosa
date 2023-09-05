package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureStarted(
    override val camera: Camera,
    val jobId: Long,
    val amount: Int,
    val exposureTime: Long,
    val captureTime: Long,
    val looping: Boolean,
) : CameraCaptureEvent
