package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureStarted(override val camera: Camera, val exposureCount: Int) : CameraCaptureEvent
