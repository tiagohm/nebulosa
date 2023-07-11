package nebulosa.api.data.events

import nebulosa.api.services.CameraExposureTask
import nebulosa.indi.device.camera.CameraEvent

data class CameraCaptureFinished(val task: CameraExposureTask) : CameraEvent {

    override val device
        get() = task.camera
}
