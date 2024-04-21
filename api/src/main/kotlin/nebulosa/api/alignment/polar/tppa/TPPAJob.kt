package nebulosa.api.alignment.polar.tppa

import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.mount.MountEvent

data class TPPAJob(override val task: TPPATask) : Job() {

    override val name = "Three-Point Polar Alignment Job"

    fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }

    fun handleMountEvent(event: MountEvent) {
        task.handleMountEvent(event)
    }
}
