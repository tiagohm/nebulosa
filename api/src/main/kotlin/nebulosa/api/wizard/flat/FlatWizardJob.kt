package nebulosa.api.wizard.flat

import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent

data class FlatWizardJob(override val task: FlatWizardTask) : Job() {

    override val name = "${task.camera.name} Flat Wizard Job"

    fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }
}
