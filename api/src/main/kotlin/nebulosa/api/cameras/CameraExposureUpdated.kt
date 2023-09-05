package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureUpdated(
    override val camera: Camera,
    val jobId: Long,
    val amount: Int,
    val remainingAmount: Int,
    val exposureTime: Long,
    val exposureRemainingTime: Long,
    val exposureProgress: Double,
    val captureTime: Long,
    val captureRemainingTime: Long,
    val captureProgress: Double,
    val looping: Boolean,
    val elapsedTime: Long,
) : CameraCaptureEvent
