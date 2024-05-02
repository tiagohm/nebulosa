package nebulosa.api.sequencer

import nebulosa.api.tasks.Job
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheelEvent

data class SequencerJob(override val task: SequencerTask) : Job() {

    override val name = "${task.camera.name} Sequencer Job"

    fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }

    fun handleFilterWheelEvent(event: FilterWheelEvent) {
        task.handleFilterWheelEvent(event)
    }
}
