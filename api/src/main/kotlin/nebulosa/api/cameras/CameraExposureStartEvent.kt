package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureStartEvent(val camera: Camera, val exposureCount: Int)
