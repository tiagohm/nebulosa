package nebulosa.api.cameras

import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path
import kotlin.time.Duration

interface CameraCaptureListener {

    fun onCameraCaptureStarted(camera: Camera) = Unit

    fun onCameraCaptureFinished(camera: Camera) = Unit

    fun onCameraExposureStarted(camera: Camera, exposureCount: Int) = Unit

    fun onCameraExposureUpdated(
        camera: Camera,
        exposureAmount: Int, exposureCount: Int,
        exposureTime: Duration, exposureRemainingTime: Duration, exposureProgress: Double,
        captureTime: Duration, captureRemainingTime: Duration, captureProgress: Double,
        looping: Boolean, elapsedTime: Duration,
    ) = Unit

    fun onCameraExposureDelayElapsed(camera: Camera, waitProgress: Double, waitRemainingTime: Duration, waitTime: Duration) = Unit

    fun onCameraExposureFinished(camera: Camera, image: Image?, path: Path?) = Unit
}
