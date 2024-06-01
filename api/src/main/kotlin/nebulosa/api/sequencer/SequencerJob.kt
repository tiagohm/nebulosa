package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.tasks.Job
import nebulosa.api.wheels.WheelEventAware
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheelEvent

data class SequencerJob(override val task: SequencerTask) : Job(), CameraEventAware, WheelEventAware {

    override val name = "${task.camera.name} Sequencer Job"

    override fun handleCameraEvent(event: CameraEvent) {
        task.handleCameraEvent(event)
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        task.handleFilterWheelEvent(event)
    }
}
