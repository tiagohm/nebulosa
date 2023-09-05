package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureFinished(override val camera: Camera) : CameraCaptureEvent {

    override val status = CameraCaptureStatus.IDLE
}
