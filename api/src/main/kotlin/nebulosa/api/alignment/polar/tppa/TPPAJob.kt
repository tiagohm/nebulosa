package nebulosa.api.alignment.polar.tppa

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class TPPAJob(override val task: TPPATask) : Job(), CameraEventAware {

    override val name = "${task.camera.name} TPPA Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
