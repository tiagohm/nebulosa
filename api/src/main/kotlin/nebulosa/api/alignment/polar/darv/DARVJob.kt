package nebulosa.api.alignment.polar.darv

import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class DARVJob(override val task: DARVTask) : Job() {

    override val name = "DARV Job"

    fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
