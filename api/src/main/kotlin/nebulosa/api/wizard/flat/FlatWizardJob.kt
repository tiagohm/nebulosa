package nebulosa.api.wizard.flat

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class FlatWizardJob(override val task: FlatWizardTask) : Job(), CameraEventAware {

    override val name = "${task.camera.name} Flat Wizard Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
