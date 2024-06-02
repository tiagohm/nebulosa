package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.focuser.FocuserEvent

data class AutoFocusJob(override val task: AutoFocusTask) : Job(), CameraEventAware, FocuserEventAware {

    override val name = "${task.camera.name} Auto Focus Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        task.handleFocuserEvent(event)
    }
}
