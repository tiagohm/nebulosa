package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class DARVJob(override val task: DARVTask) : Job(), CameraEventAware {

    override val name = "${task.camera.name} DARV Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
