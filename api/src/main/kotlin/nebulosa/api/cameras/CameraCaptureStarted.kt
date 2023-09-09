package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureStarted(override val camera: Camera) : CameraCaptureEvent
