package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureUpdated(
    override val camera: Camera,
    val jobId: Long,
    val exposureAmount: Int,
    val exposureCount: Int,
    val exposureTime: Long,
    val exposureRemainingTime: Long,
    val exposureProgress: Double,
    val captureTime: Long,
    val captureRemainingTime: Long,
    val captureProgress: Double,
    val looping: Boolean,
    val elapsedTime: Long,
    val waitProgress: Double,
    val waitRemainingTime: Long,
    override val status: CameraCaptureStatus,
) : CameraCaptureEvent
