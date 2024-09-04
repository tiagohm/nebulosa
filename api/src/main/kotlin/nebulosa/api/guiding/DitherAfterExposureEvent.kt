package nebulosa.api.guiding

import nebulosa.job.manager.TaskEvent

sealed interface DitherAfterExposureEvent : TaskEvent {

    override val task: DitherAfterExposureTask

    val dx: Double

    val dy: Double
}
