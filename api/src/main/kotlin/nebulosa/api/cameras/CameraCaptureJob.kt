package nebulosa.api.cameras

import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class CameraCaptureJob(override val task: CameraCaptureTask) : Job(), CameraEventAware {

    override val name = "${task.camera.name} Camera Capture Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
