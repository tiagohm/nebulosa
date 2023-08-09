package nebulosa.api.data.events

import nebulosa.api.services.CameraExposureTask
import nebulosa.indi.device.camera.CameraEvent

data class CameraCaptureFinished(override val task: CameraExposureTask) : TaskEvent, CameraEvent {

    override val device
        get() = task.camera
}
