package nebulosa.api.cameras

import nebulosa.api.tasks.Job
import nebulosa.api.wheels.WheelEventAware
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheelEvent

data class CameraCaptureJob(override val task: CameraCaptureTask) : Job(), CameraEventAware, WheelEventAware {

    override val name = "${task.camera.name} Camera Capture Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        task.handleFilterWheelEvent(event)
    }
}
