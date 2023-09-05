package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

interface CameraCaptureEvent {

    val camera: Camera

    val status: CameraCaptureStatus
}
